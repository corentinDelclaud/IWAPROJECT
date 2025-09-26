import React, { PropsWithChildren } from 'react';
import { ViewStyle } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';

type Props = PropsWithChildren<{
  style?: ViewStyle;
}>;

export function GradientBackground({ children, style }: Props) {
  return (
    <LinearGradient
      colors={["#0f172c", "#401c87", "#0f172c"]}
      locations={[0, 0.5, 1]}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
      style={[{ flex: 1 }, style]}
    >
      {children}
    </LinearGradient>
  );
}


