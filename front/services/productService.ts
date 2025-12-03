// services/productService.ts
// Ce service communique avec le backend via l'API Gateway (port 8090)
// Détection automatique de la plateforme
import { Platform } from 'react-native';
import { MOCK_PRODUCTS } from './mockProductData';
import { getGameImage } from '@/utils/gameImages';

// ⚠️ MODE TEST: Mettre à true pour utiliser les données mockées, false pour utiliser le backend réel
const USE_MOCK_DATA = false;

const getBaseUrl = () => {
    if (Platform.OS === 'android') {
        return `http://${process.env.EXPO_PUBLIC_API_HOST}:8080`;
    }
    // Pour iOS simulator ou web
    return 'http://localhost:8080';
};

const API_BASE_URL = `${getBaseUrl()}/api/products`;

// Interface Product utilisée par le front-end
export interface Product {
    id: number;
    title: string;
    description: string;
    price: string;  // Format: "XX€" pour l'affichage
    game: string;
    category: string;
    provider: string;
    rating: number;
    reviews: number;
    delivery: string;
    image: any;  // Peut être une string (URI) ou un require() local
    online: boolean;
    badges: string[];
    deliveryTime?: string;
    stats?: { label: string; value: string }[];
}

// Interface des données reçues du backend
interface BackendProduct {
    idService: number;
    description: string;
    price: number;  // Float dans le backend
    game: string;   // Enum Game: VALORANT, LEAGUE_OF_LEGENDS, etc.
    serviceType: string;  // Enum ServiceType: COACHING, BOOST, etc.
    idProvider: string;  // UUID du provider Keycloak
    unique?: boolean;     // Optionnel, défaut: false
    isAvailable?: boolean; // Optionnel, défaut: true
    providerName?: string;
    imageUrl?: string;
    rating?: number;
}

/**
 * Mapper pour transformer les données du backend vers le format front-end
 * Ajoute des valeurs par défaut pour les champs manquants
 */
function mapBackendProductToFrontend(backendProduct: BackendProduct): Product {
    // Générer un titre à partir de la description ou des infos du service
    const generateTitle = () => {
        if (backendProduct.description) {
            // Prendre les 60 premiers caractères de la description comme titre
            const title = backendProduct.description.split('-')[0]?.trim() ||
                         backendProduct.description.substring(0, 60).trim();
            return title + (backendProduct.description.length > 60 ? '...' : '');
        }
        // Sinon, générer un titre basé sur le type de service et le jeu
        return `${backendProduct.serviceType || 'Service'} ${backendProduct.game || ''}`.trim();
    };

    return {
        id: backendProduct.idService,
        title: generateTitle(),
        description: backendProduct.description || 'Aucune description disponible',
        price: `${backendProduct.price}€`,  // Formatage du prix avec le symbole €
        game: backendProduct.game?.toLowerCase() || 'all',
        category: backendProduct.serviceType?.toLowerCase() || 'all',
        provider: backendProduct.providerName || `Provider ${backendProduct.idProvider}`,
        rating: backendProduct.rating || 4.5,
        reviews: 127,  // Valeur par défaut - à récupérer du backend plus tard
        delivery: '24-48h',  // Temps de livraison par défaut
        // Utiliser l'image du jeu si pas d'imageUrl fournie
        image: backendProduct.imageUrl || getGameImage(backendProduct.game),
        online: backendProduct.isAvailable !== false, // Utiliser isAvailable du backend
        badges: ['Vérifié', 'Rapide'], // Badges par défaut
        deliveryTime: '24-48h', // Temps de livraison par défaut
        stats: [
            { label: 'Commandes', value: '100+' },
            { label: 'Satisfaction', value: '98%' },
        ],
    };
}

/**
 * Récupère tous les produits depuis l'API
 * @returns Promise<Product[]> - Liste de tous les produits
 */
export async function fetchProducts(): Promise<Product[]> {
    try {
        // Mode test: utiliser les données mockées
        if (USE_MOCK_DATA) {
            console.log('🧪 Using MOCK data - fetching all products');
            await new Promise(resolve => setTimeout(resolve, 300)); // Simuler latence réseau
            console.log(`Received ${MOCK_PRODUCTS.length} mock products`);
            return MOCK_PRODUCTS.map(mapBackendProductToFrontend);
        }

        // Mode production: appel API réel
        console.log('Fetching products from:', API_BASE_URL);
        const response = await fetch(API_BASE_URL);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data: BackendProduct[] = await response.json();
        console.log(`Received ${data.length} products from backend`);
        console.log(data);

        return data.map(mapBackendProductToFrontend);
    } catch (error) {
        console.error('Error fetching products:', error);
        // En cas d'erreur, retourne un tableau vide pour ne pas casser l'interface
        return [];
    }
}

/**
 * Récupère un produit spécifique par son ID
 * @param id - L'identifiant du produit
 * @returns Promise<Product | null> - Le produit ou null si non trouvé
 */
export async function fetchProductById(id: number): Promise<Product | null> {
    try {
        // Mode test: utiliser les données mockées
        if (USE_MOCK_DATA) {
            console.log(`🧪 Using MOCK data - fetching product ${id}`);
            await new Promise(resolve => setTimeout(resolve, 200)); // Simuler latence réseau
            const mockProduct = MOCK_PRODUCTS.find(p => p.idService === id);
            if (!mockProduct) {
                console.log(`Product ${id} not found in mock data`);
                return null;
            }
            console.log(`✅ Found mock product ${id}:`, mockProduct);
            return mapBackendProductToFrontend(mockProduct);
        }

        // Mode production: appel API réel
        const url = `${API_BASE_URL}/${id}`;
        console.log(`🔍 Fetching product ${id} from:`, url);

        const response = await fetch(url);

        console.log(`📡 Response status:`, response.status);
        console.log(`📡 Response ok:`, response.ok);

        if (!response.ok) {
            const errorText = await response.text();
            console.log(`❌ Error response body:`, errorText);

            if (response.status === 404) {
                console.log(`Product ${id} not found`);
                return null;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data: BackendProduct = await response.json();
        console.log(`✅ Received product ${id}:`, data);

        return mapBackendProductToFrontend(data);
    } catch (error) {
        console.error(`❌ Error fetching product ${id}:`, error);
        return null;
    }
}

/**
 * Récupère les produits en fonction de filtres
 * @param filters - Objet contenant les critères de filtrage
 * @returns Promise<Product[]> - Liste des produits filtrés
 */
export async function fetchProductsByFilters(filters: {
    game?: string;
    type?: string;
    minPrice?: number;
    maxPrice?: number;
    idProvider?: string;  // UUID du provider
}): Promise<Product[]> {
    try {
        // Mode test: filtrer les données mockées
        if (USE_MOCK_DATA) {
            console.log('🧪 Using MOCK data - filtering products with:', filters);
            await new Promise(resolve => setTimeout(resolve, 250)); // Simuler latence réseau

            let filteredProducts = [...MOCK_PRODUCTS];

            // Filtrer par jeu
            if (filters.game && filters.game !== 'all') {
                filteredProducts = filteredProducts.filter(
                    p => p.game.toUpperCase() === filters.game!.toUpperCase()
                );
            }

            // Filtrer par type de service
            if (filters.type && filters.type !== 'all') {
                filteredProducts = filteredProducts.filter(
                    p => p.serviceType.toUpperCase() === filters.type!.toUpperCase()
                );
            }

            // Filtrer par prix min
            if (filters.minPrice !== undefined) {
                filteredProducts = filteredProducts.filter(p => p.price >= filters.minPrice!);
            }

            // Filtrer par prix max
            if (filters.maxPrice !== undefined) {
                filteredProducts = filteredProducts.filter(p => p.price <= filters.maxPrice!);
            }

            // Filtrer par provider
            if (filters.idProvider) {
                filteredProducts = filteredProducts.filter(
                    p => p.idProvider === filters.idProvider
                );
            }

            console.log(`Found ${filteredProducts.length} filtered mock products`);
            return filteredProducts.map(mapBackendProductToFrontend);
        }

        // Mode production: appel API réel
        // Construction des paramètres de requête
        const params = new URLSearchParams();

        // Le backend attend les valeurs en majuscules
        if (filters.game && filters.game !== 'all') {
            params.append('game', filters.game.toUpperCase());
        }
        if (filters.type && filters.type !== 'all') {
            params.append('type', filters.type.toUpperCase());
        }
        if (filters.minPrice !== undefined) {
            params.append('minPrice', filters.minPrice.toString());
        }
        if (filters.maxPrice !== undefined) {
            params.append('maxPrice', filters.maxPrice.toString());
        }
        if (filters.idProvider !== undefined) {
            params.append('idProvider', filters.idProvider.toString());
        }

        const url = `${API_BASE_URL}/search?${params}`;
        console.log('Fetching filtered products from:', url);

        const response = await fetch(url);
        console.log(response);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data: BackendProduct[] = await response.json();
        console.log(`Received ${data.length} filtered products`);

        return data.map(mapBackendProductToFrontend);
    } catch (error) {
        console.error('Error fetching filtered products:', error);
        return [];
    }
}

/**
 * Créer un nouveau produit
 * @param product - Les données du produit à créer
 * @returns Promise<Product | null> - Le produit créé ou null en cas d'erreur
 */
export async function createProduct(product: Omit<BackendProduct, 'idService'>): Promise<Product | null> {
    try {
        // Mode test: simuler la création
        if (USE_MOCK_DATA) {
            console.log('🧪 Using MOCK data - creating product:', product);
            await new Promise(resolve => setTimeout(resolve, 400)); // Simuler latence réseau

            // Générer un nouvel ID
            const newId = Math.max(...MOCK_PRODUCTS.map(p => p.idService)) + 1;
            const newProduct: BackendProduct = {
                ...product,
                idService: newId,
            };

            // Ajouter au tableau mock (seulement en mémoire pendant la session)
            MOCK_PRODUCTS.push(newProduct);
            console.log(`✅ Mock product created with id ${newId}`);

            return mapBackendProductToFrontend(newProduct);
        }

        // Mode production: appel API réel
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(product),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data: BackendProduct = await response.json();
        return mapBackendProductToFrontend(data);
    } catch (error) {
        console.error('Error creating product:', error);
        return null;
    }
}

/**
 * Supprimer un nouveau produit
 * @param product - Les données du produit à créer
 * @returns Promise<Product | null> - Le produit créé ou null en cas d'erreur
 */
export async function deleteProduct(id: number): Promise<boolean> {
    try {
        // Mode test: simuler la suppression
        if (USE_MOCK_DATA) {
            console.log(`🧪 Using MOCK data - deleting product ${id}`);
            await new Promise(resolve => setTimeout(resolve, 300)); // Simuler latence réseau

            const index = MOCK_PRODUCTS.findIndex(p => p.idService === id);
            if (index === -1) {
                console.log(`Product ${id} not found in mock data`);
                return false;
            }

            // Supprimer du tableau mock (seulement en mémoire pendant la session)
            MOCK_PRODUCTS.splice(index, 1);
            console.log(`✅ Mock product ${id} deleted`);
            return true;
        }

        // Mode production: appel API réel
        const url = `${API_BASE_URL}/${id}`;
        const response = await fetch(url, {
            method: 'DELETE',
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return true;
    } catch (error) {
        console.error('Error deleting product:', error);
        return false;
    }
}

/**
 * Récupère les produits d'un fournisseur spécifique
 * @param idProvider - L'identifiant du fournisseur (UUID)
 * @returns Promise<Product[]> - Liste des produits du fournisseur
 */
export async function fetchProductsByProvider(idProvider: string): Promise<Product[]> {
    try {
        const url = `${API_BASE_URL}/provider/${idProvider}`;
        console.log(`Fetching products for provider ${idProvider} from:`, url);

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data: BackendProduct[] = await response.json();
        console.log(`Received ${data.length} products for provider ${idProvider}`);

        return data.map(mapBackendProductToFrontend);
    } catch (error) {
        console.error(`Error fetching products for provider ${idProvider}:`, error);
        return [];
    }
}
