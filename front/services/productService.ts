import { Service, ServiceFilters } from '../types/service';
import { mockServices } from '../data/services';

/**
 * Service pour gérer les opérations liées aux produits/services
 * En production, ces méthodes feraient des appels API réels
 */
export class ProductService {
  /**
   * Récupère tous les services
   */
  static async getAllServices(): Promise<Service[]> {
    // Simulation d'un délai d'API
    await new Promise(resolve => setTimeout(resolve, 100));
    return mockServices;
  }

  /**
   * Récupère un service par son ID
   */
  static async getServiceById(id: number): Promise<Service | null> {
    await new Promise(resolve => setTimeout(resolve, 50));
    return mockServices.find(service => service.id === id) || null;
  }

  /**
   * Filtre les services selon les critères donnés
   */
  static filterServices(services: Service[], filters: ServiceFilters): Service[] {
    return services.filter((service) => {
      const matchesSearch =
        service.title.toLowerCase().includes(filters.searchTerm.toLowerCase()) ||
        service.provider.toLowerCase().includes(filters.searchTerm.toLowerCase());

      const matchesGame =
        filters.selectedGame === "all" || service.game === filters.selectedGame;

      const matchesCategory =
        filters.selectedCategory === "all" || service.category === filters.selectedCategory;

      return matchesSearch && matchesGame && matchesCategory;
    });
  }

  /**
   * Recherche des services par terme de recherche
   */
  static async searchServices(searchTerm: string): Promise<Service[]> {
    const allServices = await this.getAllServices();
    return allServices.filter(service =>
      service.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      service.provider.toLowerCase().includes(searchTerm.toLowerCase()) ||
      service.description.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }

  /**
   * Récupère les services d'une catégorie spécifique
   */
  static async getServicesByCategory(category: string): Promise<Service[]> {
    const allServices = await this.getAllServices();
    return allServices.filter(service => service.category === category);
  }

  /**
   * Récupère les services d'un jeu spécifique
   */
  static async getServicesByGame(game: string): Promise<Service[]> {
    const allServices = await this.getAllServices();
    return allServices.filter(service => service.game === game);
  }
}
