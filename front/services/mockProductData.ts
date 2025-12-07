// Mock data for testing without backend
export interface BackendProduct {
    idService: number;
    description: string;
    price: number;
    game: string;
    serviceType: string;
    idProvider: string;
    unique?: boolean;
    isAvailable?: boolean;
    providerName?: string;
    imageUrl?: string;
    rating?: number;
}

export const MOCK_PRODUCTS: BackendProduct[] = [
    {
        idService: 1,
        description: "Coaching personnalisé Valorant - Analyse de gameplay, stratégies avancées pour atteindre Radiant",
        price: 30.0,
        game: "VALORANT",
        serviceType: "COACHING",
        idProvider: "94ba8d62-6521-4c66-87b5-edac76514bff",
        unique: false,
        isAvailable: true,
        providerName: "ProGamer_Valorant",
        rating: 4.8
    },
    {
        idService: 2,
        description: "Boost de rang LoL (Fer à Diamant) - Service rapide et sécurisé par joueurs Master/Grandmaster",
        price: 20.0,
        game: "LEAGUE_OF_LEGENDS",
        serviceType: "BOOST",
        idProvider: "a1b2c3d4-5678-90ab-cdef-1234567890ab",
        unique: false,
        isAvailable: true,
        providerName: "LoL_Booster_Pro",
        rating: 4.9
    },
    {
        idService: 3,
        description: "Boost rapide Valorant - De Bronze à Immortal en moins d'une semaine",
        price: 45.0,
        game: "VALORANT",
        serviceType: "BOOST",
        idProvider: "94ba8d62-6521-4c66-87b5-edac76514bff",
        unique: false,
        isAvailable: true,
        providerName: "ProGamer_Valorant",
        rating: 4.7
    },
    {
        idService: 4,
        description: "Compte LoL niveau 30 avec 50 champions débloqués - Non classé, prêt pour le ranked",
        price: 25.0,
        game: "LEAGUE_OF_LEGENDS",
        serviceType: "ACCOUNT_RESALING",
        idProvider: "b2c3d4e5-6789-01bc-def2-234567890abc",
        unique: true,
        isAvailable: true,
        providerName: "AccountSeller_LoL",
        rating: 4.5
    },
    {
        idService: 5,
        description: "Coaching Rocket League - Mécanique avancée, positionnement, rotation en 2v2 et 3v3",
        price: 35.0,
        game: "ROCKET_LEAGUE",
        serviceType: "COACHING",
        idProvider: "c3d4e5f6-7890-12cd-ef34-34567890abcd",
        unique: false,
        isAvailable: true,
        providerName: "RocketLeague_Coach",
        rating: 4.6
    },
    {
        idService: 6,
        description: "Coaching TFT - Stratégies avancées, composition meta, gestion économique pour atteindre Master+",
        price: 28.0,
        game: "TEAMFIGHT_TACTICS",
        serviceType: "COACHING",
        idProvider: "d4e5f6g7-8901-23de-f456-4567890abcde",
        unique: false,
        isAvailable: true,
        providerName: "TFT_Master_Coach",
        rating: 4.7
    },
    {
        idService: 7,
        description: "Boost Rocket League - De Bronze à Champion en 48h - Joueurs SSL certifiés",
        price: 40.0,
        game: "ROCKET_LEAGUE",
        serviceType: "BOOST",
        idProvider: "c3d4e5f6-7890-12cd-ef34-34567890abcd",
        unique: false,
        isAvailable: true,
        providerName: "RocketLeague_Coach",
        rating: 4.8
    },
    {
        idService: 8,
        description: "Compte Valorant level 50 avec plusieurs agents débloqués - Aucun rank, idéal pour commencer",
        price: 30.0,
        game: "VALORANT",
        serviceType: "ACCOUNT_RESALING",
        idProvider: "e5f6g7h8-9012-34ef-g567-567890abcdef",
        unique: true,
        isAvailable: false,
        providerName: "Val_Accounts",
        rating: 4.3
    },
    {
        idService: 9,
        description: "Coaching individuel League of Legends - Focus sur votre rôle principal, VOD review inclus",
        price: 32.0,
        game: "LEAGUE_OF_LEGENDS",
        serviceType: "COACHING",
        idProvider: "a1b2c3d4-5678-90ab-cdef-1234567890ab",
        unique: false,
        isAvailable: true,
        providerName: "LoL_Booster_Pro",
        rating: 4.9
    },
    {
        idService: 10,
        description: "Boost TFT - Montée de rang rapide jusqu'à Diamant avec LP garanti",
        price: 22.0,
        game: "TEAMFIGHT_TACTICS",
        serviceType: "BOOST",
        idProvider: "d4e5f6g7-8901-23de-f456-4567890abcde",
        unique: false,
        isAvailable: true,
        providerName: "TFT_Master_Coach",
        rating: 4.6
    },
    {
        idService: 11,
        description: "Service de coaching personnalisé - Tous jeux disponibles, sessions sur mesure",
        price: 50.0,
        game: "OTHER",
        serviceType: "COACHING",
        idProvider: "f6g7h8i9-0123-45fg-h678-67890abcdefg",
        unique: false,
        isAvailable: true,
        providerName: "MultiGame_Pro",
        rating: 4.5
    },
    {
        idService: 12,
        description: "Compte Rocket League avec inventaire rare - Plusieurs items exclusifs, Platinum rank",
        price: 55.0,
        game: "ROCKET_LEAGUE",
        serviceType: "ACCOUNT_RESALING",
        idProvider: "c3d4e5f6-7890-12cd-ef34-34567890abcd",
        unique: true,
        isAvailable: true,
        providerName: "RocketLeague_Coach",
        rating: 4.4
    }
];

