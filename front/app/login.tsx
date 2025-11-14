import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/context/AuthContext';
import { Colors } from '@/constants/theme';
import { GradientBackground } from '@/components/GradientBackground';

export default function LoginScreen() {
  const { login, isLoading } = useAuth();
  const router = useRouter();
  const [isAuthenticating, setIsAuthenticating] = useState(false);

  const handleLogin = () => {
    // Confirm with the user before redirecting to Keycloak
    Alert.alert(
      'Redirection',
      "Vous allez être redirigé vers Keycloak pour vous authentifier.",
      [
        { text: 'Annuler', style: 'cancel' },
        {
          text: 'Continuer',
          onPress: async () => {
            try {
              setIsAuthenticating(true);
              await login();
              router.replace('/(tabs)');
            } catch (error) {
              Alert.alert(
                'Échec de la connexion',
                "Impossible d'authentifier. Veuillez réessayer.",
                [{ text: 'OK' }]
              );
              console.error('Login error:', error);
            } finally {
              setIsAuthenticating(false);
            }
          },
        },
      ],
      { cancelable: true }
    );
  };

  return (
    <GradientBackground>
      <View style={styles.content}>
        <View style={styles.header}>
          <Text style={styles.title}>Bienvenue sur NextLevel !</Text>
          <Text style={styles.subtitle}>
            Veuillez vous connecter pour pouvoir accéder à nos services.
          </Text>
        </View>

        <View style={styles.loginSection}>
          <TouchableOpacity
            style={[styles.loginButton, isAuthenticating && styles.loginButtonDisabled]}
            onPress={handleLogin}
            disabled={isAuthenticating || isLoading}
            accessibilityLabel="Se connecter avec Keycloak"
          >
            {isAuthenticating || isLoading ? (
              <ActivityIndicator color={Colors.dark.tint} />
            ) : (
              <Text style={styles.loginButtonText}>Se connecter</Text>
            )}
          </TouchableOpacity>

          <Text style={styles.helpText}>
            Vous allez être redirigé·e vers Keycloak pour l'authentification.
          </Text>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>
            Un problème avec la connexion ? Contactez le support.
          </Text>
        </View>
      </View>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    padding: 24,
  },
  header: {
    marginBottom: 48,
    alignItems: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: Colors.dark.text,
    marginBottom: 12,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 16,
    color: Colors.dark.text,
    textAlign: 'center',
    opacity: 0.8,
  },
  loginSection: {
    marginBottom: 32,
  },
  loginButton: {
    backgroundColor: '#401c87',
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
    borderWidth: 2,
    borderColor: Colors.dark.purple,
  },
  loginButtonDisabled: {
    opacity: 0.6,
  },
  loginButtonText: {
    color: Colors.dark.text,
    fontSize: 18,
    fontWeight: '600',
  },
  helpText: {
    marginTop: 16,
    fontSize: 14,
    color: Colors.dark.textSecondary,
    textAlign: 'center',
    opacity: 0.7,
  },
  footer: {
    marginTop: 'auto',
    alignItems: 'center',
  },
  footerText: {
    fontSize: 14,
    color: Colors.dark.textSecondary,
    textAlign: 'center',
    opacity: 0.6,
  },
});
