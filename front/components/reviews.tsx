import React, { useMemo, useState } from 'react';
import { FlatList, Pressable, Text, View } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

type ReceivedReview = {
  id: number;
  reviewer: string;
  service: string;
  rating: number;
  date: string;
  comment: string;
  helpful: number;
  game: string;
};

type GivenReview = {
  id: number;
  provider: string;
  service: string;
  rating: number;
  date: string;
  comment: string;
  game: string;
};

export default function ReviewsScreen() {
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];
  const [activeTab, setActiveTab] = useState<'received' | 'given'>('received');
  const [filterRating, setFilterRating] = useState<'all' | 1 | 2 | 3 | 4 | 5>('all');

  const receivedReviews: ReceivedReview[] = [
    { id: 1, reviewer: 'PlayerOne', service: 'Coaching Valorant', rating: 5, date: '15/01/2025', comment: "Excellent coaching ! J'ai énormément appris...", helpful: 12, game: 'Valorant' },
    { id: 2, reviewer: 'GamerX', service: 'Boost LoL', rating: 4, date: '14/01/2025', comment: "Service rapide et efficace.", helpful: 8, game: 'League of Legends' },
    { id: 3, reviewer: 'ProShooter', service: 'Carry CS2', rating: 5, date: '13/01/2025', comment: 'Incroyable ! 3 victoires consécutives comme promis.', helpful: 15, game: 'CS2' },
    { id: 4, reviewer: 'NoobPlayer', service: 'Coaching général', rating: 3, date: '12/01/2025', comment: "Correct mais j'aurais aimé plus de conseils personnalisés.", helpful: 3, game: 'Valorant' },
  ];

  const givenReviews: GivenReview[] = [
    { id: 1, provider: 'ValorantMaster', service: 'Coaching avancé', rating: 5, date: '10/01/2025', comment: "Coach exceptionnel !", game: 'Valorant' },
    { id: 2, provider: 'LeagueCarry', service: 'Boost express', rating: 4, date: '08/01/2025', comment: 'Service professionnel.', game: 'League of Legends' },
  ];

  const filteredReceived = useMemo(() => {
    if (filterRating === 'all') return receivedReviews;
    return receivedReviews.filter(r => r.rating === filterRating);
  }, [filterRating]);

  const renderStars = (rating: number) => (
    <Text style={{ color: '#FBBF24' }}>{'★'.repeat(rating)}<Text style={{ color: '#6B7280' }}>{'★'.repeat(5 - rating)}</Text></Text>
  );

  return (
    <ThemedView style={{ flex: 1, padding: 16, gap: 16 }}>
      <View style={{ backgroundColor: theme.slateCard, borderRadius: 14, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
        <ThemedText type="title">Système d'Avis</ThemedText>
        <ThemedText style={{ color: '#9CA3AF', marginTop: 4 }}>Gérez vos évaluations et retours</ThemedText>
      </View>

      <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, padding: 8 }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
          <View style={{ flexDirection: 'row', gap: 8 }}>
            {[
              { id: 'received', label: 'Avis reçus', count: receivedReviews.length },
              { id: 'given', label: 'Avis donnés', count: givenReviews.length },
            ].map((tab) => (
              <Pressable
                key={tab.id}
                onPress={() => setActiveTab(tab.id as any)}
                style={{
                  paddingVertical: 10,
                  paddingHorizontal: 14,
                  borderRadius: 10,
                  borderWidth: 1,
                  borderColor: activeTab === tab.id ? 'rgba(168,85,247,0.5)' : theme.slateBorder,
                  backgroundColor: activeTab === tab.id ? 'rgba(147,51,234,0.15)' : 'transparent',
                  flexDirection: 'row',
                  alignItems: 'center',
                  gap: 8,
                }}
              >
                <Text style={{ color: theme.text }}>{tab.label}</Text>
                <View style={{ backgroundColor: 'rgba(71,85,105,0.5)', paddingHorizontal: 8, paddingVertical: 2, borderRadius: 999 }}>
                  <Text style={{ color: theme.text, fontSize: 12 }}>{tab.count}</Text>
                </View>
              </Pressable>
            ))}
          </View>

          {activeTab === 'received' && (
            <View style={{ flexDirection: 'row', gap: 8 }}>
              {(['all', 5, 4, 3, 2, 1] as const).map((value) => (
                <Pressable
                  key={String(value)}
                  onPress={() => setFilterRating(value)}
                  style={{
                    paddingVertical: 8,
                    paddingHorizontal: 12,
                    borderRadius: 999,
                    borderWidth: 1,
                    borderColor: filterRating === value ? 'rgba(168,85,247,0.5)' : theme.slateBorder,
                    backgroundColor: filterRating === value ? 'rgba(147,51,234,0.15)' : 'transparent',
                  }}
                >
                  <Text style={{ color: theme.text, fontSize: 12 }}>{value === 'all' ? 'Toutes' : `${value}★`}</Text>
                </Pressable>
              ))}
            </View>
          )}
        </View>
      </View>

      {activeTab === 'received' ? (
        <FlatList
          data={filteredReceived}
          keyExtractor={(r) => String(r.id)}
          ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
          renderItem={({ item }) => (
            <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, padding: 16 }}>
              <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                  <View style={{ width: 40, height: 40, borderRadius: 20, backgroundColor: 'rgba(147,51,234,0.3)', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: 'white', fontWeight: '700' }}>{item.reviewer[0]}</Text>
                  </View>
                  <View>
                    <Text style={{ color: theme.text, fontWeight: '600' }}>{item.reviewer}</Text>
                    <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.service} • {item.game}</Text>
                  </View>
                </View>
                <View style={{ alignItems: 'flex-end' }}>
                  {renderStars(item.rating)}
                  <Text style={{ color: '#9CA3AF', fontSize: 12, marginTop: 4 }}>{item.date}</Text>
                </View>
              </View>
              <Text style={{ color: '#D1D5DB', marginBottom: 12 }}>{item.comment}</Text>
              <View style={{ flexDirection: 'row', gap: 16 }}>
                <Pressable>
                  <Text style={{ color: '#9CA3AF' }}>Utile ({item.helpful})</Text>
                </Pressable>
                <Pressable>
                  <Text style={{ color: '#A78BFA' }}>Répondre</Text>
                </Pressable>
              </View>
            </View>
          )}
        />
      ) : (
        <FlatList
          data={givenReviews}
          keyExtractor={(r) => String(r.id)}
          ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
          renderItem={({ item }) => (
            <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, padding: 16 }}>
              <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                  <View style={{ width: 40, height: 40, borderRadius: 20, backgroundColor: 'rgba(16,185,129,0.3)', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: 'white', fontWeight: '700' }}>{item.provider[0]}</Text>
                  </View>
                  <View>
                    <Text style={{ color: theme.text, fontWeight: '600' }}>{item.provider}</Text>
                    <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.service} • {item.game}</Text>
                  </View>
                </View>
                <View style={{ alignItems: 'flex-end' }}>
                  {renderStars(item.rating)}
                  <Text style={{ color: '#9CA3AF', fontSize: 12, marginTop: 4 }}>{item.date}</Text>
                </View>
              </View>
              <Text style={{ color: '#D1D5DB' }}>{item.comment}</Text>
            </View>
          )}
        />
      )}
    </ThemedView>
  );
}


