import { Image } from 'expo-image';
import React from 'react';
import { View, ImageSourcePropType } from 'react-native';

type Props = {
  source: string | ImageSourcePropType | any;
  width: number;
  height: number;
  borderRadius?: number;
};

export function ImageWithFallback({ source, width, height, borderRadius = 8 }: Readonly<Props>) {
  // Si source est une string, c'est une URI
  // Sinon, c'est un require() local
  const imageSource = typeof source === 'string' ? { uri: source } : source;

  return (
    <View style={{ width, height, borderRadius, overflow: 'hidden', backgroundColor: 'rgba(100,116,139,0.3)' }}>
      <Image
        source={imageSource}
        style={{ width: '100%', height: '100%' }}
        contentFit="cover"
        transition={200}
      />
    </View>
  );
}


