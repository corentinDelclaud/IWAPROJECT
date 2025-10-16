export type Product = {
  id: number;
  title: string;
  provider: string;
  game: string;
  category: string;
  price: string;
  rating: number;
  reviews: number;
  description: string;
  image: string;
  badges: string[];
  delivery: string;
  online: boolean;
};

const PRODUCTS: Product[] = [
  {
    id: 1,
    title: "Coaching Personnalisé Valorant",
    provider: "ValorantMaster",
    game: "valorant",
    category: "coaching",
    price: "30€/h",
    rating: 4.9,
    reviews: 156,
    description: "Coaching individuel pour atteindre Radiant. Analyse de gameplay, stratégies avancées.",
    image: "https://images.unsplash.com/photo-1605134550917-5fe8cf25a125?q=80&w=400",
    badges: ["Pro Player", "Radiant", "Coach Certifié"],
    delivery: "1-2 heures",
    online: true,
  },
  {
    id: 2,
    title: "Boost Rang LoL (Fer à Diamant)",
    provider: "LeagueCarry",
    game: "lol",
    category: "boost",
    price: "20€/div",
    rating: 4.7,
    reviews: 89,
    description: "Service de boost rapide et sécurisé. Joueurs Master/Grandmaster uniquement.",
    image: "https://images.unsplash.com/photo-1675310854573-c5c8e4089426?q=80&w=400",
    badges: ["Master", "Boost Vérifié"],
    delivery: "24-48h",
    online: false,
  },
];

export async function fetchProducts(): Promise<Product[]> {
  // Simuler un appel réseau. Remplacer ensuite par fetch vers l'API.
  return new Promise((resolve) => setTimeout(() => resolve(PRODUCTS), 120));
}

export async function fetchProductById(id: number): Promise<Product | undefined> {
  return new Promise((resolve) => setTimeout(() => resolve(PRODUCTS.find(p => p.id === id)), 100));
}

