import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';
import { useEffect, useState } from 'react';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';

import { useColorScheme } from '@/hooks/use-color-scheme';
import { GradientBackground } from '@/components/GradientBackground';
import LanguageSwitcher from '@/components/LanguageSwitcher';
import { Text } from 'react-native';
import { Colors } from '@/constants/theme';
import { initI18n } from '@/i18n';
import { MobileHeader } from '@/components/MobileHeader';


export const unstable_settings = {
  anchor: '(tabs)',
};

export default function RootLayout() {
  const colorScheme = useColorScheme();
  const [i18nReady, setI18nReady] = useState(false);

  useEffect(() => {
    let mounted = true;
    initI18n().then(() => {
      if (mounted) setI18nReady(true);
    });
    return () => { mounted = false; };
  }, []);

  const RNText: any = Text as any;
  RNText.defaultProps = RNText.defaultProps || {};
  RNText.defaultProps.style = [RNText.defaultProps.style, { color: Colors.dark.text }];

  if (!i18nReady) {
    return null;
  }

  return (
    <SafeAreaProvider>
      <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DarkTheme}>
        <GradientBackground>
          <SafeAreaView style={{ flex: 1 }} edges={['top', 'bottom']}>
            <MobileHeader />
            <Stack screenOptions={{ contentStyle: { backgroundColor: 'transparent' } }}>
              {/* Main tabs reflecting previous React project structure */}
              <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
            </Stack>
            <StatusBar style="auto" />
          </SafeAreaView>
        </GradientBackground>
      </ThemeProvider>
    </SafeAreaProvider>
  );
}
