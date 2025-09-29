import { Image } from 'expo-image';
import React from 'react';
import { View } from 'react-native';

type Props = {
  source: string;
  width: number;
  height: number;
  borderRadius?: number;
};

export function ImageWithFallback({ source, width, height, borderRadius = 8 }: Readonly<Props>) {
  return (
    <View style={{ width, height, borderRadius, overflow: 'hidden', backgroundColor: 'rgba(100,116,139,0.3)' }}>
      <Image
        source={{ uri: source }}
        style={{ width: '100%', height: '100%' }}
        contentFit="cover"
        transition={200}
      />
    </View>
  );
}


