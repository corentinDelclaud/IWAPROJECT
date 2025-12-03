import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';
import { useEffect, useState } from 'react';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import * as Linking from 'expo-linking';
import { Alert } from 'react-native';

import { useColorScheme } from '@/hooks/use-color-scheme';
import { GradientBackground } from '@/components/GradientBackground';
import LanguageSwitcher from '@/components/LanguageSwitcher';
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
    const { isAuthenticated, isLoading, refreshUserProfile } = useAuth();
    const segments = useSegments();
    const router = useRouter();

      // Handle deep links from Stripe onboarding
      useEffect(() => {
        const handleDeepLink = async (event: { url: string }) => {
          try {
            const parsed = Linking.parse(event.url);
            
            // Handle Stripe onboarding return
            if (parsed.path === 'stripe-onboarding' && parsed.queryParams?.success === 'true') {
              // Refresh user profile to get updated Stripe account status
              await refreshUserProfile();
              
              Alert.alert(
                'Onboarding terminé',
                'Votre compte Stripe a été configuré avec succès. Vous pouvez maintenant créer des produits.',
                [{ text: 'OK' }]
              );
            }
          } catch (error) {
            console.error('Error handling deep link:', error);
          }
        };

        // Handle initial URL if app was opened via deep link
        Linking.getInitialURL().then((url) => {
          if (url) {
            handleDeepLink({ url });
          }
        });

        // Listen for deep link events while app is running
        const subscription = Linking.addEventListener('url', handleDeepLink);

        return () => subscription.remove();
      }, [refreshUserProfile]);

      useEffect(() => {
          if (isLoading) return;

          const inAuthGroup = segments[0] === '(tabs)';
          const inPublicRoute = segments[0] === 'product' || segments[0] === 'login'; // ✅ Routes publiques autorisées

          if (!isAuthenticated && inAuthGroup) {
              console.log('[Navigation] User not authenticated, redirecting to login');
              router.replace('/login' as any);
          } else if (isAuthenticated && !inAuthGroup && !inPublicRoute) {
              console.log('[Navigation] User authenticated, redirecting to tabs');
              router.replace('/(tabs)');
          }
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
          {/* Only show header when authenticated */}
          {isAuthenticated && <MobileHeader />}
          <Stack screenOptions={{ contentStyle: { backgroundColor: 'transparent' } }}>
            <Stack.Screen name="login" options={{ headerShown: false }} />
            <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          </Stack>
          <StatusBar style="auto" />
        </SafeAreaView>
      </GradientBackground>
    );
  }
