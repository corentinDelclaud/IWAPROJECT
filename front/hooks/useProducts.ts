import { useState, useEffect, useMemo } from 'react';
import { Service, ServiceFilters } from '../types/service';
import { ProductService } from '../services/productService';

/**
 * Hook personnalisé pour gérer la liste des services avec filtrage
 */
export function useServices() {
  const [services, setServices] = useState<Service[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadServices = async () => {
      try {
        setLoading(true);
        const data = await ProductService.getAllServices();
        setServices(data);
        setError(null);
      } catch (err) {
        setError('Erreur lors du chargement des services');
        console.error('Error loading services:', err);
      } finally {
        setLoading(false);
      }
    };

    loadServices();
  }, []);

  return { services, loading, error, refetch: () => loadServices() };
}

/**
 * Hook pour gérer les filtres et le filtrage des services
 */
export function useServiceFilters(services: Service[]) {
  const [filters, setFilters] = useState<ServiceFilters>({
    searchTerm: '',
    selectedGame: 'all',
    selectedCategory: 'all'
  });

  const filteredServices = useMemo(() => {
    return ProductService.filterServices(services, filters);
  }, [services, filters]);

  const updateFilter = (key: keyof ServiceFilters, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const resetFilters = () => {
    setFilters({
      searchTerm: '',
      selectedGame: 'all',
      selectedCategory: 'all'
    });
  };

  return {
    filters,
    filteredServices,
    updateFilter,
    resetFilters,
    setSearchTerm: (term: string) => updateFilter('searchTerm', term),
    setSelectedGame: (game: string) => updateFilter('selectedGame', game),
    setSelectedCategory: (category: string) => updateFilter('selectedCategory', category)
  };
}

/**
 * Hook pour récupérer un service spécifique par ID
 */
export function useService(id: number) {
  const [service, setService] = useState<Service | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadService = async () => {
      try {
        setLoading(true);
        const data = await ProductService.getServiceById(id);
        setService(data);
        setError(data ? null : 'Service introuvable');
      } catch (err) {
        setError('Erreur lors du chargement du service');
        console.error('Error loading service:', err);
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      loadService();
    }
  }, [id]);

  return { service, loading, error };
}
