import React, { useMemo, useState } from 'react';
import { FlatList, Pressable, Text, View, ScrollView } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useTranslation } from 'react-i18next';

type Section = 'profile' | 'history' | 'notifications' | 'settings';

export default function ProfileScreen() {
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];
  const { t } = useTranslation('profile');
  const [activeSection, setActiveSection] = useState<Section>('profile');

  const userStats = [
    { label: t('stats.sold'), value: '47', change: '+12%', color: '#22c55e' },
    { label: t('stats.bought'), value: '23', change: '+5%', color: '#3b82f6' },
    { label: t('stats.averageRating'), value: '4.8', change: '+0.2', color: '#f59e0b' },
    { label: t('stats.revenue'), value: '1,240€', change: '+25%', color: '#8b5cf6' },
  ];

  const orderHistory = [
    { id: '1001', service: 'Coaching Valorant', client: 'PlayerOne', status: 'completed', amount: '30€', date: '15/01/2025' },
    { id: '1002', service: 'Boost LoL', client: 'GamerX', status: 'in-progress', amount: '60€', date: '14/01/2025' },
    { id: '1003', service: 'Carry CS2', client: 'ProShooter', status: 'completed', amount: '45€', date: '13/01/2025' },
    { id: '1004', service: 'Formation Apex', client: 'LegendPlayer', status: 'pending', amount: '25€', date: '12/01/2025' },
  ];

  const notifications = [
    { type: 'order', message: 'New order received', time: '2 min', unread: true },
    { type: 'review', message: 'New 5★ review', time: '1h', unread: true },
    { type: 'payment', message: 'Payment received: 30€', time: '3h', unread: false },
    { type: 'system', message: 'Terms updated', time: '1d', unread: false },
  ];

  const StatusPill = ({ status }: { status: string }) => {
    const map: Record<string, { bg: string; fg: string; text: string }> = {
      completed: { bg: 'rgba(34,197,94,0.15)', fg: '#22c55e', text: 'Terminé' },
      'in-progress': { bg: 'rgba(59,130,246,0.15)', fg: '#3b82f6', text: 'En cours' },
      pending: { bg: 'rgba(245,158,11,0.15)', fg: '#f59e0b', text: 'En attente' },
    };
    const s = map[status] ?? map.pending;
    return (
      <View style={{ backgroundColor: s.bg, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999 }}>
        <Text style={{ color: s.fg, fontSize: 12 }}>{s.text}</Text>
      </View>
    );
  };

  const ProfileHeader = () => (
    <View style={{ backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 14, padding: 16 }}>
      <View style={{ flexDirection: 'row', alignItems: 'center', gap: 16 }}>
        
        <View style={{ flex: 1 }}>
          <ThemedText type="title">GamerPro</ThemedText>
          <Text style={{ color: '#9CA3AF' }}>{t('header.memberSince', { date: 'Mar 2023' })}</Text>
          <View style={{ flexDirection: 'row', gap: 12, marginTop: 8 }}>
            <Text style={{ color: '#FBBF24' }}>{t('header.rating', { rating: '4.8', count: 156 })}</Text>
            <Text style={{ color: '#A78BFA' }}>{t('header.certified')}</Text>
          </View>
        </View>
        
      </View>
    </View>
  );

  const StatsGrid = () => (
    <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 12 }}>
      {userStats.map((stat, idx) => (
        <View key={idx} style={{ flexGrow: 1, flexBasis: '48%', backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16 }}>
          <Text style={{ color: '#9CA3AF', marginBottom: 6 }}>{stat.label}</Text>
          <View style={{ flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-between' }}>
            <Text style={{ color: theme.text, fontWeight: '700', fontSize: 20 }}>{stat.value}</Text>
            <Text style={{ color: stat.color }}>{stat.change}</Text>
          </View>
        </View>
      ))}
    </View>
  );

  const InfoAndSkills = () => (
    <View style={{ flexDirection: 'row', gap: 12, flexWrap: 'wrap' }}>
      <View style={{ flexGrow: 1, flexBasis: '48%', backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16 }}>
        <ThemedText type="defaultSemiBold">Informations personnelles</ThemedText>
        <View style={{ height: 10 }} />
        {[
          ['Email', 'gamerpro@email.com'],
          ['Téléphone', '+33 6 12 34 56 78'],
          ['Localisation', 'Paris, France'],
        ].map(([label, value]) => (
          <View key={label} style={{ marginBottom: 10 }}>
            <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{label}</Text>
            <Text style={{ color: theme.text }}>{value}</Text>
          </View>
        ))}
      </View>
      <View style={{ flexGrow: 1, flexBasis: '48%', backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16 }}>
        <ThemedText type="defaultSemiBold">Spécialisations</ThemedText>
        <View style={{ height: 10 }} />
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          {['Valorant', 'League of Legends', 'CS2', 'Coaching', 'Analyse gameplay'].map((skill) => (
            <View key={skill} style={{ backgroundColor: 'rgba(147,51,234,0.2)', borderColor: 'rgba(168,85,247,0.3)', borderWidth: 1, paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999 }}>
              <Text style={{ color: '#C4B5FD' }}>{skill}</Text>
            </View>
          ))}
        </View>
      </View>
    </View>
  );

  const HistoryList = () => (
    <View style={{ backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16 }}>
      <ThemedText type="defaultSemiBold">Historique des commandes</ThemedText>
      <View style={{ height: 12 }} />
      <FlatList
        data={orderHistory}
        keyExtractor={(i) => i.id}
        ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
        renderItem={({ item }) => (
          <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
            <View>
              <Text style={{ color: theme.text, fontWeight: '600' }}>{item.service}</Text>
              <Text style={{ color: '#9CA3AF', fontSize: 12 }}>#{item.id} • {item.client}</Text>
            </View>
            <View style={{ alignItems: 'flex-end' }}>
              <Text style={{ color: '#34d399', fontWeight: '700' }}>{item.amount}</Text>
              <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.date}</Text>
            </View>
            <StatusPill status={item.status} />
          </View>
        )}
      />
    </View>
  );

  const NotificationsList = () => (
    <View style={{ backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16 }}>
      <ThemedText type="defaultSemiBold">Notifications</ThemedText>
      <View style={{ height: 12 }} />
      <FlatList
        data={notifications}
        keyExtractor={(_, i) => String(i)}
        ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
        renderItem={({ item }) => (
          <View style={{ padding: 12, borderRadius: 10, borderWidth: 1, borderColor: item.unread ? 'rgba(168,85,247,0.3)' : theme.slateBorder, backgroundColor: item.unread ? 'rgba(147,51,234,0.1)' : 'rgba(51,65,85,0.3)' }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
              <Text style={{ color: theme.text }}>{item.message}</Text>
              <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.time}</Text>
            </View>
          </View>
        )}
      />
    </View>
  );
  const ModifyProfile = () => (
    <Pressable style={{ backgroundColor: '#7c3aed', paddingHorizontal: 16, paddingVertical: 10, borderRadius: 10 }}>
        <Text style={{ color: 'white', fontWeight: '600' }}>Modifier le profil</Text>
    </Pressable>
  );

  const TabsHeader = () => (
    <View style={{ backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 8 }}>
      <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
        {([
          { id: 'profile', label: 'Profil' },
          { id: 'history', label: 'Historique' },
          { id: 'notifications', label: 'Notifications' },
          { id: 'settings', label: 'Paramètres' },
        ] as const).map((tab) => (
          <Pressable
            key={tab.id}
            onPress={() => setActiveSection(tab.id)}
            style={{
              paddingVertical: 10,
              paddingHorizontal: 14,
              borderRadius: 10,
              borderWidth: 1,
              borderColor: activeSection === tab.id ? 'rgba(168,85,247,0.5)' : theme.slateBorder,
              backgroundColor: activeSection === tab.id ? 'rgba(147,51,234,0.15)' : 'transparent',
            }}
          >
            <Text style={{ color: Colors[colorScheme].text }}>{tab.label}</Text>
          </Pressable>
        ))}
      </View>
    </View>
  );

  if (activeSection === 'history') {
    return (
      <ThemedView style={{ flex: 1 }}>
        <View style={{ padding: 16, gap: 16, flex: 1 }}>
          <TabsHeader />
          <View style={{ backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16, flex: 1 }}>
            <ThemedText type="defaultSemiBold">Historique des commandes</ThemedText>
            <View style={{ height: 12 }} />
            <FlatList
              data={orderHistory}
              keyExtractor={(i) => i.id}
              ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
              renderItem={({ item }) => (
                <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
                  <View>
                    <Text style={{ color: theme.text, fontWeight: '600' }}>{item.service}</Text>
                    <Text style={{ color: '#9CA3AF', fontSize: 12 }}>#{item.id} • {item.client}</Text>
                  </View>
                  <View style={{ alignItems: 'flex-end' }}>
                    <Text style={{ color: '#34d399', fontWeight: '700' }}>{item.amount}</Text>
                    <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.date}</Text>
                  </View>
                  <StatusPill status={item.status} />
                </View>
              )}
            />
          </View>
        </View>
      </ThemedView>
    );
  }

  if (activeSection === 'notifications') {
    return (
      <ThemedView style={{ flex: 1 }}>
        <View style={{ padding: 16, gap: 16, flex: 1 }}>
          <TabsHeader />
          <View style={{ backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16, flex: 1 }}>
            <ThemedText type="defaultSemiBold">Notifications</ThemedText>
            <View style={{ height: 12 }} />
            <FlatList
              data={notifications}
              keyExtractor={(_, i) => String(i)}
              ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
              renderItem={({ item }) => (
                <View style={{ padding: 12, borderRadius: 10, borderWidth: 1, borderColor: item.unread ? 'rgba(168,85,247,0.3)' : theme.slateBorder, backgroundColor: item.unread ? 'rgba(147,51,234,0.1)' : 'rgba(51,65,85,0.3)' }}>
                  <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
                    <Text style={{ color: theme.text }}>{item.message}</Text>
                    <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.time}</Text>
                  </View>
                </View>
              )}
            />
          </View>
        </View>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={{ flex: 1 }}>
      <ScrollView contentContainerStyle={{ padding: 16, gap: 16 }}>
        <TabsHeader />

        {activeSection === 'profile' && (
          <>
            <ProfileHeader />
            <StatsGrid />
            <InfoAndSkills />
          </>
        )}
        {activeSection === 'settings' && (
          <View style={{ backgroundColor: theme.slateCard, borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, padding: 16 }}>
            <ThemedText type="defaultSemiBold">Paramètres du compte</ThemedText>
            <Text style={{ color: '#9CA3AF', marginTop: 6 }}>Section des paramètres en développement...</Text>
          </View>

        )}
      </ScrollView>
    </ThemedView>
  );
}


