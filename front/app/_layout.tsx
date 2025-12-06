import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';
import { useEffect, useState } from 'react';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';

import { useColorScheme } from '@/hooks/use-color-scheme';
import { GradientBackground } from '@/components/GradientBackground';
import { Text, ActivityIndicator, View } from 'react-native';
import { Colors } from '@/constants/theme';
import { initI18n } from '@/i18n';
import { MobileHeader } from '@/components/MobileHeader';
import { AuthProvider, useAuth } from '@/context/AuthContext';

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
    <AuthProvider>
      <SafeAreaProvider>
        <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DarkTheme}>
          <RootLayoutNav />
        </ThemeProvider>
      </SafeAreaProvider>
    </AuthProvider>
  );
}

function RootLayoutNav() {
  const { isAuthenticated, isLoading } = useAuth();
  const segments = useSegments();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;

    const inAuthGroup = segments[0] === '(tabs)';
    // ✅ Routes accessibles sans redirection (publiques ou authentifiées mais hors tabs)
    const inAllowedRoute = 
      segments[0] === 'product' || 
      segments[0] === 'login' || 
      segments[0] === 'conversation'; // ✅ Ajout de conversation

    if (!isAuthenticated && inAuthGroup) {
      console.log('[Navigation] User not authenticated, redirecting to login');
      router.replace('/login' as any);
    } else if (isAuthenticated && !inAuthGroup && !inAllowedRoute) {
      console.log('[Navigation] User authenticated, redirecting to tabs');
      router.replace('/(tabs)');
    }
    // ✅ Si on est dans une route autorisée (product, conversation), on ne redirige PAS
  }, [isAuthenticated, isLoading, segments, router]);

  if (isLoading) {
    return (
      <GradientBackground>
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
          <ActivityIndicator size="large" color={Colors.dark.tint} />
        </View>
      </GradientBackground>
    );
  }

  return (
    <GradientBackground>
      <SafeAreaView style={{ flex: 1 }} edges={['top', 'bottom']}>
        {isAuthenticated && <MobileHeader />}
        <Stack screenOptions={{ contentStyle: { backgroundColor: 'transparent' } }}>
          <Stack.Screen name="login" options={{ headerShown: false }} />
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="product" options={{ headerShown: false }} />
          <Stack.Screen name="conversation" options={{ headerShown: false }} />
        </Stack>
        <StatusBar style="auto" />
      </SafeAreaView>
    </GradientBackground>
  );
}