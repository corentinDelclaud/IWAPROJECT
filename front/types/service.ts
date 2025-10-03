export interface Service {
  id: number;
  title: string;
  provider: string;
  game: string;
  category: string;
  price: string;
  rating: number;
  reviews: number;
  description: string;
  longDescription: string;
  image: string;
  badges: string[];
  delivery: string;
  online: boolean;
  features: string[];
  providerStats: ProviderStats;
}

export interface ProviderStats {
  totalOrders: number;
  completionRate: number;
  responseTime: string;
  memberSince: string;
}

export interface GameFilter {
  id: string;
  name: string;
  icon: string;
}

export interface CategoryFilter {
  id: string;
  name: string;
}

export interface ServiceFilters {
  searchTerm: string;
  selectedGame: string;
  selectedCategory: string;
}
