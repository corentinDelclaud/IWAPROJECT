import { Link } from 'expo-router';
import { StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { useTranslation } from 'react-i18next';
import { ThemedView } from '@/components/themed-view';

export default function ModalScreen() {
  const { t } = useTranslation('modal');
  return (
    <ThemedView style={styles.container}>
      <ThemedText type="title">{t('title')}</ThemedText>
      <Link href="/" dismissTo style={styles.link}>
        <ThemedText type="link">{t('homeLink')}</ThemedText>
      </Link>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  link: {
    marginTop: 15,
    paddingVertical: 15,
  },
});
