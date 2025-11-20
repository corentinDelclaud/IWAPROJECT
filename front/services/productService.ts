// services/productService.ts
// Ce service communique avec le backend via l'API Gateway (port 8090)
// Détection automatique de la plateforme
import { Platform } from 'react-native';

const getBaseUrl = () => {
    if (Platform.OS === 'android') {
        return 'http://162.38.39.123:8080';
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
    image: string;
    online: boolean;
    badges: string[];
    deliveryTime?: string;
    stats?: { label: string; value: string }[];
}

// Interface des données reçues du backend
interface BackendProduct {
    idService: number;
    name: string;
    description: string;
    price: number;
    game: string;
    serviceType: string;
    idProvider: number;
    providerName?: string;
    imageUrl?: string;
    rating?: number;
}

/**
 * Mapper pour transformer les données du backend vers le format front-end
 * Ajoute des valeurs par défaut pour les champs manquants
 */
function mapBackendProductToFrontend(backendProduct: BackendProduct): Product {
    return {
        id: backendProduct.idService,
        title: backendProduct.name,
        description: backendProduct.description,
        price: `${backendProduct.price}€`,  // Formatage du prix avec le symbole €
        game: backendProduct.game?.toLowerCase() || 'all',
        category: backendProduct.serviceType?.toLowerCase() || 'all',
        provider: backendProduct.providerName || `Provider ${backendProduct.idProvider}`,
        rating: backendProduct.rating || 4.5,
        reviews: 127,  // Valeur par défaut - à récupérer du backend plus tard
        delivery: '24-48h',  // Temps de livraison par défaut
        image: backendProduct.imageUrl || 'https://via.placeholder.com/300x200?text=No+Image',
        online: true, // Valeur par défaut
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
    idProvider?: number;
}): Promise<Product[]> {
    try {
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
 * @param idProvider - L'identifiant du fournisseur
 * @returns Promise<Product[]> - Liste des produits du fournisseur
 */
export async function fetchProductsByProvider(idProvider: number): Promise<Product[]> {
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
