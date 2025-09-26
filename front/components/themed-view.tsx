import { View, type ViewProps } from 'react-native';

import { useThemeColor } from '@/hooks/use-theme-color';

export type ThemedViewProps = ViewProps & {
  lightColor?: string;
  darkColor?: string;
};

export function ThemedView({ style, lightColor, darkColor, ...otherProps }: ThemedViewProps) {
  const backgroundColor = useThemeColor({ light: lightColor, dark: darkColor }, 'background');

  // Default to transparent unless an explicit light/dark color is provided
  const baseStyle = (lightColor || darkColor) ? { backgroundColor } : {};

  return <View style={[baseStyle, style]} {...otherProps} />;
}
