import React from 'react';
import { Modal, View, Text, TouchableOpacity, StyleSheet, Linking } from 'react-native';
import * as Clipboard from 'expo-clipboard';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

interface StripeOnboardingModalProps {
  visible: boolean;
  url: string | null;
  onClose: () => void;
  onRecheck?: () => void; // Callback pour revérifier le compte
}

export function StripeOnboardingModal({ visible, url, onClose, onRecheck }: StripeOnboardingModalProps) {
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];

  const handleCopy = async () => {
    if (!url) return;
    await Clipboard.setStringAsync(url);
  };

  const handleOpen = async () => {
    if (!url) return;
    await Linking.openURL(url);
  };

  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <View style={[styles.container, { backgroundColor: theme.background }]}>
          <Text style={[styles.title, { color: theme.text }]}>
            Activation du compte Stripe
          </Text>
          <Text style={[styles.text, { color: theme.text }]}>
            Ouvrez ce lien dans votre navigateur pour terminer l'onboarding Stripe,
            puis revenez dans l'application et réessayez d'ajouter un produit.
          </Text>

          {url && (
            <TouchableOpacity onPress={handleOpen}>
              <Text style={styles.link} selectable>
                {url}
              </Text>
            </TouchableOpacity>
          )}

          <View style={styles.buttons}>
            <TouchableOpacity style={[styles.button, { backgroundColor: theme.tint }]} onPress={handleCopy}>
              <Text>Copier le lien</Text>
            </TouchableOpacity>
            
          </View>

          {onRecheck && (
            <View style={styles.buttons}>

            <TouchableOpacity 
              style={[styles.button, { backgroundColor: '#10b981' }]} 
              onPress={onRecheck}
            >
              <Text>✓ J'ai terminé, revérifier mon compte</Text>
            </TouchableOpacity>
            </View>
          )}
          <View style={styles.buttons}>

          <TouchableOpacity 
            style={[styles.button, styles.closeButton, { backgroundColor: theme.slateCard }]} 
            onPress={onClose}
          >
            <Text style={[styles.buttonText, { color: theme.text }]}>Fermer</Text>
          </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.6)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  container: {
    width: '90%',
    borderRadius: 16,
    padding: 20,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    marginBottom: 10,
  },
  text: {
    fontSize: 14,
    marginBottom: 16,
  },
  link: {
    color: '#60a5fa',
    textDecorationLine: 'underline',
    marginBottom: 16,
    fontSize: 12,
  },
  buttons: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 12,
  },
  button: {
    flex: 1,
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  recheckButton: {
    marginTop: 4,
    marginBottom: 8,
  },
  closeButton: {
    marginTop: 4,
  },
  buttonText: {
    fontWeight: '600',
  },
  buttonTextWhite: {
    color: 'white',
    fontWeight: '600',
  },
});
