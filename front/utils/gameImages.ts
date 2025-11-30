// Helper to get game-specific images
export const getGameImage = (game?: string) => {
  // Normaliser: convertir en majuscules et gérer les cas spéciaux
  const key = (game || 'OTHER').toUpperCase().trim();

  switch (key) {
    case 'LEAGUE_OF_LEGENDS':
    case 'LEAGUE OF LEGENDS':
    case 'LOL':
      return require('@/assets/images/Game/league_of_legends.png');

    case 'TEAMFIGHT_TACTICS':
    case 'TEAMFIGHT TACTICS':
    case 'TFT':
      return require('@/assets/images/Game/tft.png');

    case 'ROCKET_LEAGUE':
    case 'ROCKET LEAGUE':
    case 'RL':
      return require('@/assets/images/Game/Rocket_League.png');

    case 'VALORANT':
    case 'VAL':
      return require('@/assets/images/Game/Valorant.png');

    case 'OTHER':
    case 'ALL':
    default:
      return require('@/assets/images/Game/other.png');
  }
};

