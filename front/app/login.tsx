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

export default function LoginScreen() {
  const { login, isLoading } = useAuth();
  const router = useRouter();
  const [isAuthenticating, setIsAuthenticating] = useState(false);

  const handleLogin = async () => {
    try {
      setIsAuthenticating(true);
      await login();
      router.replace('/(tabs)');
    } catch (error) {
      Alert.alert(
        'Login Failed',
        'Unable to authenticate with Keycloak. Please try again.',
        [{ text: 'OK' }]
      );
      console.error('Login error:', error);
    } finally {
      setIsAuthenticating(false);
    }
  };

  return (
    <LinearGradient
      colors={[Colors.dark.gradientStart, Colors.dark.gradientEnd]}
      style={styles.container}
    >
      <View style={styles.content}>
        <View style={styles.header}>
          <Text style={styles.title}>Welcome to IWA</Text>
          <Text style={styles.subtitle}>
            Sign in with your Keycloak account to continue
          </Text>
        </View>

        <View style={styles.loginSection}>
          <TouchableOpacity
            style={[styles.loginButton, isAuthenticating && styles.loginButtonDisabled]}
            onPress={handleLogin}
            disabled={isAuthenticating || isLoading}
          >
            {isAuthenticating || isLoading ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.loginButtonText}>Sign In with Keycloak</Text>
            )}
          </TouchableOpacity>

          <Text style={styles.helpText}>
            You will be redirected to Keycloak for secure authentication
          </Text>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>
            Don't have an account? Register in Keycloak Admin Console
          </Text>
        </View>
      </View>
    </LinearGradient>
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
    color: Colors.dark.textSecondary,
    textAlign: 'center',
    opacity: 0.8,
  },
  loginSection: {
    marginBottom: 32,
  },
  loginButton: {
    backgroundColor: Colors.dark.tint,
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  loginButtonDisabled: {
    opacity: 0.6,
  },
  loginButtonText: {
    color: '#fff',
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
