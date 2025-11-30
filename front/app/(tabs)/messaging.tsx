import React, { useEffect, useState, useCallback } from 'react';
import { FlatList, Pressable, Text, TextInput, View, ActivityIndicator, RefreshControl } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { fetchMyTransactions, Transaction, TransactionState } from '@/services/transactionService';

const getStateDisplay = (state: TransactionState): { label: string; color: string } => {
  const stateMap: Record<TransactionState, { label: string; color: string }> = {
    EXCHANGING: { label: 'En discussion', color: '#60A5FA' },
    REQUESTED: { label: 'Demande envoyée', color: '#FBBF24' },
    REQUEST_ACCEPTED: { label: 'Demande acceptée', color: '#34D399' },
    PREPAID: { label: 'Prépayé', color: '#A78BFA' },
    CLIENT_CONFIRMED: { label: 'Confirmé (client)', color: '#F472B6' },
    PROVIDER_CONFIRMED: { label: 'Confirmé (vendeur)', color: '#F472B6' },
    DOUBLE_CONFIRMED: { label: 'Double confirmation', color: '#34D399' },
    FINISHED_AND_PAYED: { label: 'Terminé', color: '#10B981' },
    CANCELED: { label: 'Annulé', color: '#EF4444' },
  };
  return stateMap[state] || { label: state, color: '#9CA3AF' };
};

const getGameIcon = (game?: string): string => {
  const gameIcons: Record<string, string> = {
    LEAGUE_OF_LEGENDS: '⚔️',
    TEAMFIGHT_TACTICS: '🛡️',
    ROCKET_LEAGUE: '🚗',
    VALORANT: '🎯',
    OTHER: '🎮',
  };
  return gameIcons[game?.toUpperCase() || ''] || '🎮';
};

export default function MessagingScreen() {
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];
  const { t } = useTranslation('messaging');
  const router = useRouter();
  const { isAuthenticated, userInfo } = useAuth();

  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  const loadTransactions = useCallback(async () => {
    if (!isAuthenticated) {
      setTransactions([]);
      setLoading(false);
      return;
    }

    try {
      const data = await fetchMyTransactions();
      setTransactions(data);
    } catch (error) {
      console.error('Error loading transactions:', error);
      setTransactions([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    loadTransactions();
  }, [loadTransactions]);

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    loadTransactions();
  }, [loadTransactions]);

  const filteredTransactions = transactions.filter((tx) => {
    if (!searchTerm) return true;
    const search = searchTerm.toLowerCase();
    return (
      tx.productTitle?.toLowerCase().includes(search) ||
      tx.providerName?.toLowerCase().includes(search) ||
      tx.state.toLowerCase().includes(search)
    );
  });

  const isUserClient = (tx: Transaction): boolean => {
    return tx.idClient === userInfo?.sub;
  };

  const handleNavigateToConversation = (transactionId: number) => {
    console.log('[Messaging] Navigating to conversation:', transactionId);
    router.push(`/conversation/${transactionId}` as any);
  };

  if (!isAuthenticated) {
    return (
      <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 }}>
        <Text style={{ fontSize: 48, marginBottom: 16 }}>🔒</Text>
        <ThemedText type="title" style={{ textAlign: 'center', marginBottom: 8 }}>
          Connexion requise
        </ThemedText>
        <ThemedText style={{ color: '#9CA3AF', textAlign: 'center', marginBottom: 24 }}>
          Connectez-vous pour voir vos conversations et transactions.
        </ThemedText>
        <Pressable
          onPress={() => router.push('/login')}
          style={{
            backgroundColor: '#7c3aed',
            paddingVertical: 14,
            paddingHorizontal: 32,
            borderRadius: 12,
          }}
        >
          <Text style={{ color: 'white', fontWeight: '600', fontSize: 16 }}>Se connecter</Text>
        </Pressable>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={{ flex: 1 }}>
      <View style={{ padding: 16 }}>
        <ThemedText type="title" style={{ marginBottom: 16 }}>
          {t('title') || 'Mes transactions'}
        </ThemedText>

        <TextInput
          placeholder={t('search') || 'Rechercher...'}
          placeholderTextColor="#9CA3AF"
          value={searchTerm}
          onChangeText={setSearchTerm}
          style={{
            backgroundColor: 'rgba(51,65,85,0.5)',
            borderWidth: 1,
            borderColor: theme.slateBorder,
            borderRadius: 10,
            paddingHorizontal: 12,
            paddingVertical: 10,
            color: theme.text,
            marginBottom: 12,
          }}
        />
      </View>

      {loading ? (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
          <ActivityIndicator size="large" color={theme.tint} />
        </View>
      ) : (
        <FlatList
          data={filteredTransactions}
          keyExtractor={(tx) => String(tx.id)}
          contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 32 }}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={theme.tint} />
          }
          ListEmptyComponent={
            <View style={{ padding: 40, alignItems: 'center' }}>
              <Text style={{ fontSize: 48, marginBottom: 16 }}>📭</Text>
              <ThemedText style={{ color: '#9CA3AF', textAlign: 'center' }}>
                Aucune transaction trouvée.
              </ThemedText>
            </View>
          }
          ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
          renderItem={({ item: tx }) => {
            const stateDisplay = getStateDisplay(tx.state);
            const isClient = isUserClient(tx);
            const otherPartyName = isClient ? tx.providerName : tx.clientName;
            const roleLabel = isClient ? '🛒 Acheteur' : '🏪 Vendeur';
            const roleColor = isClient ? '#60A5FA' : '#34D399';

            return (
              <Pressable
                onPress={() => handleNavigateToConversation(tx.id)}
                style={{
                  backgroundColor: theme.slateCard,
                  borderRadius: 12,
                  padding: 16,
                  borderWidth: 1,
                  borderColor: theme.slateBorder,
                }}
              >
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                  <View
                    style={{
                      width: 48,
                      height: 48,
                      borderRadius: 24,
                      backgroundColor: 'rgba(147,51,234,0.2)',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <Text style={{ fontSize: 24 }}>{getGameIcon(tx.game)}</Text>
                  </View>

                  <View style={{ flex: 1 }}>
                    {/* Titre du service */}
                    <Text
                      style={{ color: theme.text, fontWeight: '600', fontSize: 16 }}
                      numberOfLines={1}
                    >
                      {tx.productTitle || `Service #${tx.serviceId}`}
                    </Text>
                    
                    {/* Rôle de l'utilisateur */}
                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 4 }}>
                      <Text style={{ color: roleColor, fontSize: 12, fontWeight: '600' }}>
                        {roleLabel}
                      </Text>
                      {tx.price && (
                        <Text style={{ color: '#34D399', fontSize: 12, fontWeight: '600' }}>
                          • {tx.price}
                        </Text>
                      )}
                    </View>
                    
                    {/* Autre partie */}
                    <Text style={{ color: '#9CA3AF', fontSize: 13, marginTop: 2 }}>
                      {isClient ? '👤 Vendeur' : '👤 Client'}: {otherPartyName || 'Utilisateur'}
                    </Text>
                    
                    {/* État et date */}
                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 6 }}>
                      <View
                        style={{
                          backgroundColor: `${stateDisplay.color}20`,
                          paddingHorizontal: 8,
                          paddingVertical: 3,
                          borderRadius: 6,
                        }}
                      >
                        <Text style={{ color: stateDisplay.color, fontSize: 11, fontWeight: '600' }}>
                          {stateDisplay.label}
                        </Text>
                      </View>
                      <Text style={{ color: '#6B7280', fontSize: 11 }}>
                        {new Date(tx.creationDate).toLocaleDateString('fr-FR')}
                      </Text>
                    </View>
                  </View>

                  <Text style={{ color: '#9CA3AF', fontSize: 18 }}>›</Text>
                </View>
              </Pressable>
            );
          }}
        />
      )}
    </ThemedView>
  );
}


