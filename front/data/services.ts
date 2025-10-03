import { Service, GameFilter, CategoryFilter } from '../types/service';

export const mockServices: Service[] = [
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
    longDescription: "Service de coaching professionnel pour Valorant avec un coach certifié Radiant. Sessions individuelles d'une heure incluant l'analyse de vos parties, correction des erreurs, stratégies d'équipe et conseils pour améliorer votre aim. Méthode éprouvée avec plus de 200 joueurs accompagnés vers des rangs supérieurs.",
    image: "https://images.unsplash.com/photo-1605134550917-5fe8cf25a125?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxnYW1pbmclMjBzZXR1cCUyMGNvbG9yZnVsJTIwbmVvbnxlbnwxfHx8fDE3NTg3ODEwODd8MA&ixlib=rb-4.1.0&q=80&w=400",
    badges: ["Pro Player", "Radiant", "Coach Certifié"],
    delivery: "1-2 heures",
    online: true,
    features: [
      "Analyse de gameplay en direct",
      "Stratégies personnalisées",
      "Conseils pour l'amélioration de l'aim",
      "Replay review inclus",
      "Support Discord 24/7"
    ],
    providerStats: {
      totalOrders: 342,
      completionRate: 98,
      responseTime: "< 1h",
      memberSince: "2022"
    }
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
    longDescription: "Service de boost professionnel pour League of Legends. Nos boosters sont tous Master/Grandmaster avec un taux de victoire supérieur à 85%. Boost sécurisé avec VPN, stream en direct disponible sur demande. Garantie de remboursement si objectif non atteint.",
    image: "https://images.unsplash.com/photo-1675310854573-c5c8e4089426?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxlc3BvcnRzJTIwZ2FtaW5nJTIwcHJvZmVzc2lvbmFsfGVufDF8fHx8MTc1ODc4MTA5MHww&ixlib=rb-4.1.0&q=80&w=400",
    badges: ["Master", "Boost Vérifié"],
    delivery: "24-48h",
    online: false,
    features: [
      "Boost sécurisé avec VPN",
      "Stream disponible",
      "Boosters Master/GM",
      "Taux de victoire 85%+",
      "Garantie remboursement"
    ],
    providerStats: {
      totalOrders: 156,
      completionRate: 95,
      responseTime: "< 2h",
      memberSince: "2023"
    }
  },
  {
    id: 3,
    title: "Coaching CS2 Aim Training",
    provider: "CS2Pro",
    game: "cs2",
    category: "coaching",
    price: "25€/h",
    rating: 4.8,
    reviews: 73,
    description: "Amélioration de l'aim et du positioning pour CS2.",
    longDescription: "Coaching spécialisé Counter-Strike 2 avec focus sur l'amélioration de l'aim, le positioning et les stratégies d'équipe. Sessions incluant aim training personnalisé, analyse de demos et conseils tactiques.",
    image: "https://images.unsplash.com/photo-1542751371-adc38448a05e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxnYW1pbmclMjBzZXR1cCUyMGNvbG9yZnVsJTIwbmVvbnxlbnwxfHx8fDE3NTg3ODEwODd8MA&ixlib=rb-4.1.0&q=80&w=400",
    badges: ["Global Elite", "FACEIT Level 10"],
    delivery: "2-3 heures",
    online: true,
    features: [
      "Aim training personnalisé",
      "Analyse de demos",
      "Conseils tactiques",
      "Maps callouts training",
      "1v1 practice sessions"
    ],
    providerStats: {
      totalOrders: 89,
      completionRate: 96,
      responseTime: "< 30min",
      memberSince: "2023"
    }
  }
];

export const gameFilters: GameFilter[] = [
  { id: "all", name: "Tous les jeux", icon: "🎮" },
  { id: "lol", name: "League of Legends", icon: "⚔️" },
  { id: "valorant", name: "Valorant", icon: "🎯" },
  { id: "cs2", name: "CS2", icon: "💥" },
  { id: "fortnite", name: "Fortnite", icon: "🌪️" },
  { id: "apex", name: "Apex Legends", icon: "🚁" },
];

export const categoryFilters: CategoryFilter[] = [
  { id: "all", name: "Toutes catégories" },
  { id: "coaching", name: "Coaching" },
  { id: "boost", name: "Boost" },
  { id: "carry", name: "Carry" },
  { id: "account", name: "Comptes" },
  { id: "items", name: "Items" },
];
