import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';

import { useColorScheme } from '@/hooks/use-color-scheme';
import { GradientBackground } from '@/components/GradientBackground';
import { Text } from 'react-native';
import { Colors } from '@/constants/theme';

export const unstable_settings = {
  anchor: '(tabs)',
};

export default function RootLayout() {
  const colorScheme = useColorScheme();

  const RNText: any = Text as any;
  RNText.defaultProps = RNText.defaultProps || {};
  RNText.defaultProps.style = [RNText.defaultProps.style, { color: Colors.dark.text }];

  return (
    <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DarkTheme}>
      <GradientBackground>
      <Stack screenOptions={{ contentStyle: { backgroundColor: 'transparent' } }}>
        {/* Main tabs reflecting previous React project structure */}
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="dashboard" options={{ title: 'Dashboard' }} />
        <Stack.Screen name="marketplace" options={{ title: 'Marketplace' }} />
        <Stack.Screen name="messaging" options={{ title: 'Messaging' }} />
        <Stack.Screen name="orders" options={{ title: 'Orders' }} />
        <Stack.Screen name="reviews" options={{ title: 'Reviews' }} />
        <Stack.Screen name="profile" options={{ title: 'Profile' }} />
      </Stack>
      <StatusBar style="auto" />
      </GradientBackground>
    </ThemeProvider>
  );
}
