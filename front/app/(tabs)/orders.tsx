import React, { useMemo, useState } from 'react';
import { FlatList, Pressable, Text, View } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { MobileHeader } from '@/components/MobileHeader';

type Order = {
  id: string;
  service: string;
  client: string;
  provider: string;
  status: 'pending' | 'in-progress' | 'completed' | 'cancelled';
  amount: string;
  date: string;
  deliveryDate: string;
  description: string;
  type: 'purchase' | 'sale';
  rating?: number;
};

export default function OrdersScreen() {
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];
  const [activeTab, setActiveTab] = useState<'all' | 'purchases' | 'sales'>('all');

  const orders: Order[] = [
    { id: 'ORD-1001', service: 'Coaching Valorant Personnalisé', client: 'PlayerOne', provider: 'ValorantMaster', status: 'in-progress', amount: '30€', date: '15/01/2025', deliveryDate: '16/01/2025', description: 'Session de coaching 1h pour améliorer les stratégies et le positionnement', type: 'purchase' },
    { id: 'ORD-1002', service: 'Boost Rang LoL', client: 'GamerX', provider: 'LeagueCarry', status: 'completed', amount: '60€', date: '14/01/2025', deliveryDate: '15/01/2025', description: 'Boost de Platine 3 à Diamant 4', type: 'sale', rating: 5 },
    { id: 'ORD-1003', service: 'Carry CS2 Premier', client: 'ProShooter', provider: 'CS2Elite', status: 'pending', amount: '45€', date: '13/01/2025', deliveryDate: '13/01/2025', description: '3 matchs Premier Mode avec garantie de victoire', type: 'purchase' },
    { id: 'ORD-1004', service: 'Formation Apex Legends', client: 'LegendPlayer', provider: 'ApexCoach', status: 'cancelled', amount: '25€', date: '12/01/2025', deliveryDate: '13/01/2025', description: 'Session annulée par le client', type: 'sale' },
  ];

  const filteredOrders = useMemo(() => {
    if (activeTab === 'all') return orders;
    if (activeTab === 'purchases') return orders.filter(o => o.type === 'purchase');
    return orders.filter(o => o.type === 'sale');
  }, [activeTab]);

  const getStatusChip = (status: Order['status']) => {
    const map = {
      pending: { text: 'En attente', color: 'rgba(250,204,21,0.15)', fg: '#f59e0b' },
      'in-progress': { text: 'En cours', color: 'rgba(59,130,246,0.15)', fg: '#3b82f6' },
      completed: { text: 'Terminé', color: 'rgba(34,197,94,0.15)', fg: '#22c55e' },
      cancelled: { text: 'Annulé', color: 'rgba(239,68,68,0.15)', fg: '#ef4444' },
    } as const;
    const s = map[status];
    return (
      <View style={{ flexDirection: 'row', alignItems: 'center', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999, backgroundColor: s.color }}>
        <Text style={{ color: s.fg, fontSize: 12, fontWeight: '600' }}>{s.text}</Text>
      </View>
    );
  };

  return (
    <ThemedView style={{ flex: 1 }}>
      <MobileHeader />
      <ThemedView style={{ padding: 16, gap: 16 }}>
      <View style={{ backgroundColor: theme.slateCard, borderRadius: 14, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
        <ThemedText type="title">Gestion des Commandes</ThemedText>
        <ThemedText style={{ color: '#9CA3AF', marginTop: 4 }}>Suivez et gérez toutes vos commandes</ThemedText>
      </View>

      <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, padding: 8 }}>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          {[
            { id: 'all', label: 'Toutes', count: orders.length },
            { id: 'purchases', label: 'Achats', count: orders.filter(o => o.type === 'purchase').length },
            { id: 'sales', label: 'Ventes', count: orders.filter(o => o.type === 'sale').length },
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
      </View>

      <FlatList
        data={filteredOrders}
        keyExtractor={(o) => o.id}
        ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
        renderItem={({ item }) => (
          <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, padding: 16 }}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
              <View>
                <Text style={{ color: theme.text, fontWeight: '600', fontSize: 16 }}>{item.service}</Text>
                <Text style={{ color: '#9CA3AF', marginTop: 2, fontSize: 12 }}>
                  {item.type === 'purchase' ? `Prestataire: ${item.provider}` : `Client: ${item.client}`} • #{item.id}
                </Text>
              </View>
              <View style={{ alignItems: 'flex-end' }}>
                <Text style={{ color: '#34d399', fontWeight: '700', fontSize: 16 }}>{item.amount}</Text>
                <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.date}</Text>
              </View>
            </View>

            <Text style={{ color: '#D1D5DB', fontSize: 12, marginBottom: 10 }}>{item.description}</Text>

            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
              {getStatusChip(item.status)}
              <Text style={{ color: '#9CA3AF', fontSize: 12 }}>Livraison: {item.deliveryDate}</Text>
              {item.rating && (
                <Text style={{ color: '#FBBF24', fontSize: 12 }}>{'★'.repeat(item.rating)}</Text>
              )}
            </View>

            <View style={{ flexDirection: 'row', gap: 8, marginTop: 12 }}>
              <Pressable style={{ paddingVertical: 10, paddingHorizontal: 12, backgroundColor: 'rgba(71,85,105,0.5)', borderRadius: 10, borderWidth: 1, borderColor: theme.slateBorder }}>
                <Text style={{ color: theme.text }}>Détails</Text>
              </Pressable>
              <Pressable style={{ paddingVertical: 10, paddingHorizontal: 12, backgroundColor: 'rgba(147,51,234,0.2)', borderRadius: 10, borderWidth: 1, borderColor: 'rgba(168,85,247,0.3)' }}>
                <Text style={{ color: '#C4B5FD' }}>Message</Text>
              </Pressable>
              {item.status === 'in-progress' && (
                <Pressable style={{ paddingVertical: 10, paddingHorizontal: 12, backgroundColor: '#16a34a', borderRadius: 10 }}>
                  <Text style={{ color: 'white' }}>Marquer terminé</Text>
                </Pressable>
              )}
              {item.status === 'completed' && !item.rating && item.type === 'purchase' && (
                <Pressable style={{ paddingVertical: 10, paddingHorizontal: 12, backgroundColor: '#f59e0b', borderRadius: 10 }}>
                  <Text style={{ color: 'white' }}>Laisser un avis</Text>
                </Pressable>
              )}
            </View>
          </View>
        )}
      />
      </ThemedView>
    </ThemedView>
  );
}


