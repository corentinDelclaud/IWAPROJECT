import React, { useEffect, useState, useCallback, useRef } from 'react';
import {
  View,
  ScrollView,
  Text,
  TextInput,
  Pressable,
  ActivityIndicator,
  Alert,
  RefreshControl,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { useLocalSearchParams, useRouter, Stack } from 'expo-router';
import { ThemedView } from '@/components/themed-view';
import { ThemedText } from '@/components/themed-text';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useAuth } from '@/context/AuthContext';
import {
  fetchTransactionById,
  updateTransactionState,
  subscribeToTransactionUpdates,
  Transaction,
  TransactionState,
} from '@/services/transactionService';

interface StateConfig {
  label: string;
  color: string;
  description: string;
}

const STATE_CONFIGS: Record<TransactionState, StateConfig> = {
  EXCHANGING: {
    label: 'En discussion',
    color: '#60A5FA',
    description: 'Vous pouvez discuter des détails du service.',
  },
  REQUESTED: {
    label: 'Demande envoyée',
    color: '#FBBF24',
    description: 'En attente de la réponse du vendeur.',
  },
  REQUEST_ACCEPTED: {
    label: 'Demande acceptée',
    color: '#34D399',
    description: 'Le vendeur a accepté. Procédez au paiement.',
  },
  PREPAID: {
    label: 'Prépayé',
    color: '#A78BFA',
    description: 'Paiement reçu. Service en cours de réalisation.',
  },
  CLIENT_CONFIRMED: {
    label: 'Confirmé par le client',
    color: '#F472B6',
    description: 'En attente de la confirmation du vendeur.',
  },
  PROVIDER_CONFIRMED: {
    label: 'Confirmé par le vendeur',
    color: '#F472B6',
    description: 'En attente de la confirmation du client.',
  },
  DOUBLE_CONFIRMED: {
    label: 'Double confirmation',
    color: '#34D399',
    description: 'Transaction en cours de finalisation.',
  },
  FINISHED_AND_PAYED: {
    label: 'Terminé',
    color: '#10B981',
    description: 'Transaction terminée avec succès.',
  },
  CANCELED: {
    label: 'Annulé',
    color: '#EF4444',
    description: 'Cette transaction a été annulée.',
  },
};

interface ActionButton {
  label: string;
  targetState: TransactionState;
  color: string;
  confirmation?: string;
}

export default function ConversationPage() {
  const params = useLocalSearchParams();
  const rawId = params?.id;
  const router = useRouter();
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];
  const { isAuthenticated, userInfo } = useAuth();

  const [transaction, setTransaction] = useState<Transaction | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [messageText, setMessageText] = useState('');

  const unsubscribeRef = useRef<(() => void) | null>(null);

  const transactionId = rawId ? Number(rawId) : NaN;

  console.log('[ConversationPage] Rendering with id:', rawId, 'parsed:', transactionId);

  const loadTransaction = useCallback(async () => {
    if (Number.isNaN(transactionId)) {
      console.log('[ConversationPage] Invalid transaction ID');
      setLoading(false);
      return;
    }

    try {
      console.log('[ConversationPage] Loading transaction:', transactionId);
      const data = await fetchTransactionById(transactionId);
      console.log('[ConversationPage] Transaction loaded:', data);
      setTransaction(data);
    } catch (error) {
      console.error('[ConversationPage] Error loading transaction:', error);
      setTransaction(null);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [transactionId]);

  useEffect(() => {
    loadTransaction();
  }, [loadTransaction]);

  // SSE subscription
  useEffect(() => {
    if (!isAuthenticated || Number.isNaN(transactionId)) return;

    console.log('[ConversationPage][SSE] Setting up subscription for transaction:', transactionId);

    const unsubscribe = subscribeToTransactionUpdates(
      transactionId,
      (updatedTransaction) => {
        console.log('[ConversationPage][SSE] Received update:', updatedTransaction);
        setTransaction(updatedTransaction);
      },
      (error) => {
        console.error('[ConversationPage][SSE] Error:', error);
      }
    );

    unsubscribeRef.current = unsubscribe;

    return () => {
      console.log('[ConversationPage][SSE] Cleaning up subscription');
      if (unsubscribeRef.current) {
        unsubscribeRef.current();
        unsubscribeRef.current = null;
      }
    };
  }, [isAuthenticated, transactionId]);

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    loadTransaction();
  }, [loadTransaction]);

  const isClient = transaction?.idClient === userInfo?.sub;
  const isProvider = transaction?.idProvider === userInfo?.sub;

  const getAvailableActions = (): ActionButton[] => {
    if (!transaction) return [];

    const state = transaction.state;
    const actions: ActionButton[] = [];

    if (state === 'EXCHANGING' && isClient) {
      actions.push({
        label: 'Demander la réservation',
        targetState: 'REQUESTED',
        color: '#7c3aed',
        confirmation: 'Voulez-vous envoyer une demande de réservation au vendeur ?',
      });
    }

    if (state === 'REQUESTED' && isProvider) {
      actions.push({
        label: 'Accepter la demande',
        targetState: 'REQUEST_ACCEPTED',
        color: '#10B981',
        confirmation: 'Voulez-vous accepter cette demande ?',
      });
    }

    if (state === 'REQUEST_ACCEPTED' && isClient) {
      actions.push({
        label: 'Procéder au paiement',
        targetState: 'PREPAID',
        color: '#7c3aed',
        confirmation: 'Confirmez-vous le paiement pour ce service ?',
      });
    }

    if (state === 'PREPAID') {
      if (isClient) {
        actions.push({
          label: 'Confirmer la réception',
          targetState: 'CLIENT_CONFIRMED',
          color: '#10B981',
          confirmation: 'Confirmez-vous avoir reçu le service ?',
        });
      }
      if (isProvider) {
        actions.push({
          label: 'Confirmer la livraison',
          targetState: 'PROVIDER_CONFIRMED',
          color: '#10B981',
          confirmation: 'Confirmez-vous avoir livré le service ?',
        });
      }
    }

    if (state === 'CLIENT_CONFIRMED' && isProvider) {
      actions.push({
        label: 'Confirmer la livraison',
        targetState: 'PROVIDER_CONFIRMED',
        color: '#10B981',
        confirmation: 'Confirmez-vous avoir livré le service ?',
      });
    }

    if (state === 'PROVIDER_CONFIRMED' && isClient) {
      actions.push({
        label: 'Confirmer la réception',
        targetState: 'CLIENT_CONFIRMED',
        color: '#10B981',
        confirmation: 'Confirmez-vous avoir reçu le service ?',
      });
    }

    // Cancel option
    if (!['FINISHED_AND_PAYED', 'CANCELED', 'DOUBLE_CONFIRMED'].includes(state)) {
      actions.push({
        label: 'Annuler la transaction',
        targetState: 'CANCELED',
        color: '#EF4444',
        confirmation: 'Êtes-vous sûr de vouloir annuler cette transaction ?',
      });
    }

    return actions;
  };

  const handleAction = async (action: ActionButton) => {
    if (!transaction) return;

    if (action.confirmation) {
      Alert.alert('Confirmation', action.confirmation, [
        { text: 'Non', style: 'cancel' },
        {
          text: 'Oui',
          onPress: () => executeAction(action.targetState),
        },
      ]);
    } else {
      executeAction(action.targetState);
    }
  };

  const executeAction = async (targetState: TransactionState) => {
    if (!transaction) return;

    setUpdating(true);
    try {
      const updated = await updateTransactionState(transaction.id, targetState);
      setTransaction(updated);
      Alert.alert('Succès', 'La transaction a été mise à jour.');
    } catch (error: any) {
      console.error('Error updating transaction:', error);
      Alert.alert('Erreur', error.message || 'Impossible de mettre à jour la transaction.');
    } finally {
      setUpdating(false);
    }
  };

  // Afficher la page de chargement
  if (loading) {
    return (
      <>
        <Stack.Screen options={{ headerShown: false }} />
        <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
          <ActivityIndicator size="large" color={theme.tint} />
          <ThemedText style={{ marginTop: 8 }}>Chargement...</ThemedText>
        </ThemedView>
      </>
    );
  }

  // Non authentifié
  if (!isAuthenticated) {
    return (
      <>
        <Stack.Screen options={{ headerShown: false }} />
        <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 }}>
          <ThemedText type="title">Connexion requise</ThemedText>
          <Pressable
            onPress={() => router.push('/login')}
            style={{
              marginTop: 16,
              backgroundColor: '#7c3aed',
              paddingVertical: 12,
              paddingHorizontal: 24,
              borderRadius: 10,
            }}
          >
            <Text style={{ color: 'white', fontWeight: '600' }}>Se connecter</Text>
          </Pressable>
        </ThemedView>
      </>
    );
  }

  // Transaction non trouvée
  if (!transaction) {
    return (
      <>
        <Stack.Screen options={{ headerShown: false }} />
        <ThemedView style={{ flex: 1, padding: 16 }}>
          <ThemedText type="title">Transaction introuvable</ThemedText>
          <ThemedText style={{ color: '#9CA3AF', marginTop: 8 }}>
            ID demandé: {rawId}
          </ThemedText>
          <Pressable
            onPress={() => router.back()}
            style={{
              marginTop: 16,
              backgroundColor: theme.tint,
              paddingVertical: 12,
              paddingHorizontal: 24,
              borderRadius: 10,
              alignSelf: 'flex-start',
            }}
          >
            <Text style={{ color: 'white', fontWeight: '600' }}>Retour</Text>
          </Pressable>
        </ThemedView>
      </>
    );
  }

  const stateConfig = STATE_CONFIGS[transaction.state];
  const actions = getAvailableActions();
  const roleLabel = isClient ? 'Acheteur' : 'Vendeur';
  const roleColor = isClient ? '#60A5FA' : '#34D399';
  const otherPartyLabel = isClient ? 'Vendeur' : 'Client';
  const otherPartyName = isClient 
    ? (transaction.providerName || 'Vendeur') 
    : (transaction.clientName || 'Client');

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <ThemedView style={{ flex: 1 }}>
        <KeyboardAvoidingView 
          style={{ flex: 1 }} 
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        >
          <ScrollView
            contentContainerStyle={{ padding: 16, paddingBottom: 100 }}
            refreshControl={
              <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={theme.tint} />
            }
          >
            {/* Header */}
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 16 }}>
              <Pressable 
                onPress={() => router.back()}
                style={{
                  width: 40,
                  height: 40,
                  borderRadius: 20,
                  backgroundColor: theme.slateCard,
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Text style={{ color: theme.text, fontSize: 20 }}>←</Text>
              </Pressable>
              <View style={{ flex: 1 }}>
                <ThemedText type="subtitle" numberOfLines={1}>
                  {transaction.productTitle || `Service #${transaction.serviceId}`}
                </ThemedText>
                <Text style={{ color: '#9CA3AF', fontSize: 12 }}>
                  Transaction #{transaction.id}
                </Text>
              </View>
            </View>

            {/* Rôle et autre partie */}
            <View
              style={{
                backgroundColor: theme.slateCard,
                borderRadius: 12,
                padding: 16,
                borderWidth: 1,
                borderColor: theme.slateBorder,
                marginBottom: 12,
              }}
            >
              <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                <View>
                  <Text style={{ color: '#9CA3AF', fontSize: 12 }}>Votre rôle</Text>
                  <Text style={{ color: roleColor, fontWeight: '700', fontSize: 16, marginTop: 2 }}>
                    {isClient ? '🛒' : '🏪'} {roleLabel}
                  </Text>
                </View>
                <View style={{ alignItems: 'flex-end' }}>
                  <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{otherPartyLabel}</Text>
                  <Text style={{ color: theme.text, fontWeight: '600', fontSize: 14, marginTop: 2 }}>
                    👤 {otherPartyName}
                  </Text>
                </View>
              </View>
              
              {transaction.price && (
                <View style={{ marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderTopColor: theme.slateBorder }}>
                  <Text style={{ color: '#9CA3AF', fontSize: 12 }}>Prix</Text>
                  <Text style={{ color: '#34D399', fontWeight: '700', fontSize: 20, marginTop: 2 }}>
                    {transaction.price}
                  </Text>
                </View>
              )}
            </View>

            {/* State Card */}
            <View
              style={{
                backgroundColor: theme.slateCard,
                borderRadius: 12,
                padding: 16,
                borderWidth: 1,
                borderColor: theme.slateBorder,
                marginBottom: 12,
              }}
            >
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 12 }}>
                <View
                  style={{
                    backgroundColor: `${stateConfig.color}20`,
                    paddingHorizontal: 12,
                    paddingVertical: 6,
                    borderRadius: 8,
                  }}
                >
                  <Text style={{ color: stateConfig.color, fontWeight: '700', fontSize: 14 }}>
                    {stateConfig.label}
                  </Text>
                </View>
              </View>
              <Text style={{ color: '#9CA3AF', fontSize: 14, lineHeight: 20 }}>
                {stateConfig.description}
              </Text>
            </View>

            {/* Timeline */}
            <View
              style={{
                backgroundColor: theme.slateCard,
                borderRadius: 12,
                padding: 16,
                borderWidth: 1,
                borderColor: theme.slateBorder,
                marginBottom: 12,
              }}
            >
              <ThemedText type="subtitle" style={{ marginBottom: 12 }}>
                Historique
              </ThemedText>
              <View style={{ gap: 8 }}>
                <TimelineItem
                  label="Création"
                  date={transaction.creationDate}
                  completed={true}
                  theme={theme}
                />
                <TimelineItem
                  label="Validation demande"
                  date={transaction.requestValidationDate}
                  completed={!!transaction.requestValidationDate}
                  theme={theme}
                />
                <TimelineItem
                  label="Finalisation"
                  date={transaction.finishDate}
                  completed={!!transaction.finishDate}
                  theme={theme}
                />
              </View>
            </View>

            {/* Actions */}
            {actions.length > 0 && (
              <View style={{ gap: 12, marginBottom: 12 }}>
                <ThemedText type="subtitle">Actions disponibles</ThemedText>
                {actions.map((action, index) => (
                  <Pressable
                    key={index}
                    onPress={() => handleAction(action)}
                    disabled={updating}
                    style={{
                      backgroundColor: action.color,
                      paddingVertical: 14,
                      paddingHorizontal: 20,
                      borderRadius: 10,
                      alignItems: 'center',
                      opacity: updating ? 0.6 : 1,
                    }}
                  >
                    {updating ? (
                      <ActivityIndicator color="white" size="small" />
                    ) : (
                      <Text style={{ color: 'white', fontWeight: '700', fontSize: 15 }}>
                        {action.label}
                      </Text>
                    )}
                  </Pressable>
                ))}
              </View>
            )}
          </ScrollView>

          {/* Message input (disabled) */}
          <View
            style={{
              position: 'absolute',
              bottom: 0,
              left: 0,
              right: 0,
              backgroundColor: theme.slateCard,
              borderTopWidth: 1,
              borderTopColor: theme.slateBorder,
              padding: 12,
              flexDirection: 'row',
              alignItems: 'center',
              gap: 12,
            }}
          >
            <TextInput
              placeholder="Messagerie bientôt disponible..."
              placeholderTextColor="#6B7280"
              value={messageText}
              onChangeText={setMessageText}
              editable={false}
              style={{
                flex: 1,
                backgroundColor: 'rgba(55,65,81,0.5)',
                borderRadius: 20,
                paddingHorizontal: 16,
                paddingVertical: 10,
                color: theme.text,
                opacity: 0.5,
              }}
            />
            <Pressable
              disabled={true}
              style={{
                width: 44,
                height: 44,
                borderRadius: 22,
                backgroundColor: '#4B5563',
                alignItems: 'center',
                justifyContent: 'center',
                opacity: 0.5,
              }}
            >
              <Text style={{ fontSize: 20 }}>📤</Text>
            </Pressable>
          </View>
        </KeyboardAvoidingView>
      </ThemedView>
    </>
  );
}

function TimelineItem({
  label,
  date,
  completed,
  theme,
}: {
  label: string;
  date?: string;
  completed: boolean;
  theme: any;
}) {
  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
      <View
        style={{
          width: 12,
          height: 12,
          borderRadius: 6,
          backgroundColor: completed ? '#10B981' : '#374151',
        }}
      />
      <View style={{ flex: 1 }}>
        <Text style={{ color: completed ? theme.text : '#6B7280', fontWeight: '500' }}>{label}</Text>
        {date && (
          <Text style={{ color: '#9CA3AF', fontSize: 12 }}>
            {new Date(date).toLocaleDateString('fr-FR', {
              day: 'numeric',
              month: 'short',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </Text>
        )}
      </View>
    </View>
  );
}