import React, { useMemo, useState } from 'react';
import { FlatList, Pressable, Text, TextInput, View } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { MobileHeader } from '@/components/MobileHeader';

type Conversation = {
  id: number;
  name: string;
  avatar: string;
  lastMessage: string;
  timestamp: string;
  unread: number;
  online: boolean;
  service: string;
};

type Message = {
  id: number;
  senderId: number;
  content: string;
  timestamp: string;
};

export default function MessagingScreen() {
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];

  const [selectedChat, setSelectedChat] = useState<number>(1);
  const [newMessage, setNewMessage] = useState<string>('');

  const conversations: Conversation[] = [
    { id: 1, name: 'ProGamer23', avatar: '🎮', lastMessage: 'Parfait ! Merci pour le coaching', timestamp: '14:30', unread: 0, online: true, service: 'Coaching Valorant' },
    { id: 2, name: 'LeagueCarry', avatar: '⚔️', lastMessage: 'Je peux commencer le boost maintenant', timestamp: '13:45', unread: 2, online: true, service: 'Boost LoL' },
    { id: 3, name: 'CS2Elite', avatar: '💥', lastMessage: 'Session terminée, merci !', timestamp: '12:20', unread: 0, online: false, service: 'Carry CS2' },
    { id: 4, name: 'FortniteAcademy', avatar: '🌪️', lastMessage: "Quand est-ce qu'on programme la prochaine session ?", timestamp: '11:15', unread: 1, online: true, service: 'Formation Fortnite' },
  ];

  const messages: Message[] = [
    { id: 1, senderId: 2, content: 'Salut ! Je suis intéressé par ton service de coaching Valorant', timestamp: '14:00' },
    { id: 2, senderId: 1, content: "Bonjour ! Bien sûr, je peux t'aider. Quel est ton rang actuel ?", timestamp: '14:02' },
    { id: 3, senderId: 2, content: "Je suis actuellement Platine 2, j'aimerais atteindre Diamant", timestamp: '14:05' },
    { id: 4, senderId: 1, content: 'Parfait ! Je peux t\'aider à analyser ton gameplay et améliorer ta stratégie. Quand es-tu disponible ?', timestamp: '14:07' },
    { id: 5, senderId: 2, content: "Je suis libre ce soir vers 20h, ça te va ?", timestamp: '14:10' },
    { id: 6, senderId: 1, content: "C'est noté ! Je t'enverrai les détails de connexion Discord vers 19h45", timestamp: '14:12' },
    { id: 7, senderId: 2, content: 'Parfait ! Merci pour le coaching', timestamp: '14:30' },
  ];

  const currentChat = useMemo(() => conversations.find(c => c.id === selectedChat), [selectedChat]);

  const sendMessage = () => {
    if (!newMessage.trim()) return;
    setNewMessage('');
  };

  return (
    <ThemedView style={{ flex: 1 }}>
      <MobileHeader />
      <ThemedView style={{ borderColor: theme.slateBorder, borderWidth: 1, borderRadius: 12, backgroundColor: theme.slateCard, overflow: 'hidden', margin: 12 }}>
      <View style={{ flexDirection: 'row', height: '100%' }}>
        <View style={{ width: '100%', maxWidth: 380, borderRightWidth: 1, borderColor: theme.slateBorder }}>
          <View style={{ padding: 12, borderBottomWidth: 1, borderColor: theme.slateBorder }}>
            <TextInput
              placeholder="Rechercher..."
              placeholderTextColor="#9CA3AF"
              style={{ backgroundColor: 'rgba(51,65,85,0.5)', borderWidth: 1, borderColor: theme.slateBorder, borderRadius: 10, paddingHorizontal: 12, paddingVertical: 10, color: theme.text }}
            />
          </View>

          <FlatList
            data={conversations}
            keyExtractor={(c) => String(c.id)}
            renderItem={({ item }) => (
              <Pressable
                onPress={() => setSelectedChat(item.id)}
                style={{ padding: 12, borderBottomWidth: 1, borderColor: 'rgba(51,65,85,0.3)', backgroundColor: selectedChat === item.id ? 'rgba(147,51,234,0.1)' : 'transparent' }}
              >
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                  <View style={{ position: 'relative' }}>
                    <View style={{ width: 40, height: 40, borderRadius: 20, backgroundColor: 'linear-gradient(90deg, #9333ea, #2563eb)' as any, alignItems: 'center', justifyContent: 'center' }}>
                      <Text style={{ color: 'white', fontSize: 16 }}>{item.avatar}</Text>
                    </View>
                    {item.online && (
                      <View style={{ position: 'absolute', bottom: -2, right: -2, width: 10, height: 10, backgroundColor: theme.green, borderRadius: 10, borderWidth: 2, borderColor: theme.slateCard }} />
                    )}
                  </View>

                  <View style={{ flex: 1 }}>
                    <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 2 }}>
                      <Text style={{ color: theme.text, fontWeight: '600' }} numberOfLines={1}>{item.name}</Text>
                      <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.timestamp}</Text>
                    </View>
                    <Text style={{ color: '#9CA3AF', fontSize: 12 }} numberOfLines={1}>{item.lastMessage}</Text>
                    <Text style={{ color: '#a78bfa', fontSize: 12, marginTop: 2 }}>{item.service}</Text>
                  </View>

                  {item.unread > 0 && (
                    <View style={{ width: 16, height: 16, borderRadius: 8, backgroundColor: '#7c3aed', alignItems: 'center', justifyContent: 'center' }}>
                      <Text style={{ color: 'white', fontSize: 10 }}>{item.unread}</Text>
                    </View>
                  )}
                </View>
              </Pressable>
            )}
          />
        </View>

        <View style={{ flex: 1, display: 'none' }} />
      </View>
    </ThemedView>
    </ThemedView>
  );
}


