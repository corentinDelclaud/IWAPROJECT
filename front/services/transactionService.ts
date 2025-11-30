import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import EventSource from 'react-native-sse';
import { fetchProductById, Product } from './productService';

const getBaseUrl = () => {
    const host = process.env.EXPO_PUBLIC_API_HOST || 'localhost';
    if (Platform.OS === 'android') {
        return `http://${host}:8080`;
    }
    return `http://${host}:8080`;
};

const API_BASE_URL = `${getBaseUrl()}/api/transactions`;

// Types pour les transactions
export type TransactionState = 
    | 'EXCHANGING'
    | 'REQUESTED'
    | 'REQUEST_ACCEPTED'
    | 'PREPAID'
    | 'CLIENT_CONFIRMED'
    | 'PROVIDER_CONFIRMED'
    | 'DOUBLE_CONFIRMED'
    | 'FINISHED_AND_PAYED'
    | 'CANCELED';

export interface Transaction {
    id: number;
    state: TransactionState;
    serviceId: number;
    idClient: string;
    idProvider: string;
    creationDate: string;
    requestValidationDate?: string;
    finishDate?: string;
    // Champs enrichis
    productTitle?: string;
    providerName?: string;
    clientName?: string;
    game?: string;
    price?: string;
}

export interface CreateTransactionRequest {
    serviceId: number;
    directRequest: boolean;
}

export interface UpdateStateRequest {
    newState: TransactionState;
}

// Cache pour les produits
const productCache = new Map<number, Product>();

// Helper pour récupérer le token
async function getAuthHeaders(): Promise<Record<string, string>> {
    const token = await AsyncStorage.getItem('@auth/access_token');
    if (!token) {
        throw new Error('No access token available');
    }
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };
}

/**
 * Enrichir une transaction avec les infos du produit
 */
async function enrichTransaction(tx: Transaction): Promise<Transaction> {
    try {
        let product = productCache.get(tx.serviceId);
        
        if (!product) {
            const fetched = await fetchProductById(tx.serviceId);
            if (fetched) {
                product = fetched;
                productCache.set(tx.serviceId, product);
            }
        }
        
        if (product) {
            return {
                ...tx,
                productTitle: product.title,
                providerName: product.provider,
                game: product.game?.toUpperCase(),
                price: product.price,
            };
        }
    } catch (error) {
        console.warn('[Transaction] Failed to enrich transaction:', tx.id, error);
    }
    return tx;
}

/**
 * Créer une nouvelle transaction
 */
export async function createTransaction(request: CreateTransactionRequest): Promise<Transaction> {
    try {
        const headers = await getAuthHeaders();
        console.log('[Transaction] Creating transaction for service:', request.serviceId);
        
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers,
            body: JSON.stringify(request),
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('[Transaction] Create failed:', response.status, errorText);
            throw new Error(`Failed to create transaction: ${response.status} - ${errorText}`);
        }

        const data = await response.json();
        console.log('[Transaction] Created:', data);
        const mapped = mapBackendTransaction(data);
        return await enrichTransaction(mapped);
    } catch (error) {
        console.error('[Transaction] Create error:', error);
        throw error;
    }
}

/**
 * Récupérer une transaction par ID
 */
export async function fetchTransactionById(id: number): Promise<Transaction> {
    try {
        const headers = await getAuthHeaders();
        console.log('[Transaction] Fetching transaction:', id);
        
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'GET',
            headers,
        });

        if (!response.ok) {
            throw new Error(`Failed to get transaction: ${response.status}`);
        }

        const data = await response.json();
        const mapped = mapBackendTransaction(data);
        return await enrichTransaction(mapped);
    } catch (error) {
        console.error('[Transaction] Get error:', error);
        throw error;
    }
}

/**
 * Récupérer toutes les transactions de l'utilisateur connecté
 */
export async function fetchMyTransactions(): Promise<Transaction[]> {
    try {
        const headers = await getAuthHeaders();
        console.log('[Transaction] Fetching my transactions');
        
        const response = await fetch(`${API_BASE_URL}/my`, {
            method: 'GET',
            headers,
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('[Transaction] Fetch my transactions failed:', response.status, errorText);
            throw new Error(`Failed to get transactions: ${response.status}`);
        }

        const data = await response.json();
        console.log('[Transaction] My transactions:', data.length);
        
        const mapped = data.map(mapBackendTransaction);
        const enriched = await Promise.all(mapped.map(enrichTransaction));
        
        return enriched;
    } catch (error) {
        console.error('[Transaction] Get my transactions error:', error);
        throw error;
    }
}

/**
 * Mettre à jour l'état d'une transaction
 */
export async function updateTransactionState(
    transactionId: number, 
    newState: TransactionState
): Promise<Transaction> {
    try {
        const headers = await getAuthHeaders();
        console.log('[Transaction] Updating state:', transactionId, '->', newState);
        
        const response = await fetch(`${API_BASE_URL}/${transactionId}/state`, {
            method: 'PUT',
            headers,
            body: JSON.stringify({ newState }),
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('[Transaction] Update state failed:', response.status, errorText);
            throw new Error(`Failed to update state: ${response.status} - ${errorText}`);
        }

        const data = await response.json();
        console.log('[Transaction] State updated:', data);
        const mapped = mapBackendTransaction(data);
        return await enrichTransaction(mapped);
    } catch (error) {
        console.error('[Transaction] Update state error:', error);
        throw error;
    }
}

/**
 * Mapper la réponse backend vers le format frontend
 */
function mapBackendTransaction(data: any): Transaction {
    return {
        id: data.id,
        state: data.state,
        serviceId: data.serviceId,
        idClient: data.idClient,
        idProvider: data.idProvider,
        creationDate: data.creationDate,
        requestValidationDate: data.requestValidationDate,
        finishDate: data.finishDate,
        productTitle: data.productTitle,
        providerName: data.providerName,
        clientName: data.clientName,
        game: data.game,
    };
}

/**
 * S'abonner aux mises à jour SSE d'une transaction
 */
export function subscribeToTransactionUpdates(
    transactionId: number,
    onUpdate: (transaction: Transaction) => void,
    onError?: (error: Error) => void
): () => void {
    let eventSource: EventSource | null = null;
    let isClosing = false;

    const connect = async () => {
        try {
            const token = await AsyncStorage.getItem('@auth/access_token');
            if (!token) {
                console.warn('[SSE] No token available');
                onError?.(new Error('No token available'));
                return;
            }

            const url = `${getBaseUrl()}/api/transactions/sse/${transactionId}`;
            console.log('[SSE] Connecting to:', url);

            eventSource = new EventSource(url, {
                headers: {
                    Authorization: {
                        toString: () => `Bearer ${token}`,
                    },
                },
            });

            eventSource.addEventListener('open', () => {
                console.log('[SSE] Connection opened for transaction:', transactionId);
            });

            eventSource.addEventListener('message', async (event: any) => {
                if (event.data && !isClosing) {
                    try {
                        const transaction = JSON.parse(event.data);
                        // Ignorer les heartbeats
                        if (transaction.id && transaction.id !== -1 && transaction.type !== 'heartbeat') {
                            console.log('[SSE] Received update:', transaction);
                            const mapped = mapBackendTransaction(transaction);
                            const enriched = await enrichTransaction(mapped);
                            onUpdate(enriched);
                        }
                    } catch (parseError) {
                        console.warn('[SSE] Parse error:', parseError);
                    }
                }
            });

            eventSource.addEventListener('error', (event: any) => {
                if (!isClosing) {
                    console.error('[SSE] Error:', event);
                    onError?.(new Error(event.message || 'SSE connection error'));
                }
            });

        } catch (error) {
            console.error('[SSE] Connection error:', error);
            onError?.(error as Error);
        }
    };

    connect();

    return () => {
        isClosing = true;
        if (eventSource) {
            console.log('[SSE] Closing connection');
            eventSource.removeAllEventListeners();
            eventSource.close();
            eventSource = null;
        }
    };
}

/**
 * Obtenir le label français d'un état
 */
export function getStateLabel(state: TransactionState): string {
    const labels: Record<TransactionState, string> = {
        EXCHANGING: 'En discussion',
        REQUESTED: 'Demande envoyée',
        REQUEST_ACCEPTED: 'Demande acceptée',
        PREPAID: 'Prépaiement effectué',
        CLIENT_CONFIRMED: 'Confirmé par le client',
        PROVIDER_CONFIRMED: 'Confirmé par le vendeur',
        DOUBLE_CONFIRMED: 'Double confirmation',
        FINISHED_AND_PAYED: 'Terminé et payé',
        CANCELED: 'Annulé',
    };
    return labels[state] || state;
}

/**
 * Obtenir la couleur associée à un état
 */
export function getStateColor(state: TransactionState): string {
    const colors: Record<TransactionState, string> = {
        EXCHANGING: '#60A5FA',
        REQUESTED: '#FBBF24',
        REQUEST_ACCEPTED: '#34D399',
        PREPAID: '#A78BFA',
        CLIENT_CONFIRMED: '#34D399',
        PROVIDER_CONFIRMED: '#34D399',
        DOUBLE_CONFIRMED: '#10B981',
        FINISHED_AND_PAYED: '#10B981',
        CANCELED: '#EF4444',
    };
    return colors[state] || '#9CA3AF';
}

/**
 * Vider le cache des produits
 */
export function clearProductCache(): void {
    productCache.clear();
}