import React, { useMemo, useState } from "react";
import { FlatList, TextInput, View, Text, Pressable } from "react-native";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { MobileHeader } from "@/components/MobileHeader";

type Service = {
  id: number;
  title: string;
  provider: string;
  game: string;
  category: string;
  price: string;
  rating: number;
  reviews: number;
  description: string;
  image: string;
  badges: string[];
  delivery: string;
  online: boolean;
};

export default function MarketplaceScreen() {
  const colorScheme = useColorScheme() ?? "light";
  const theme = Colors[colorScheme];
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedGame, setSelectedGame] = useState("all");
  const [selectedCategory, setSelectedCategory] = useState("all");

  const games = [
    { id: "all", name: "Tous les jeux", icon: "🎮" },
    { id: "lol", name: "League of Legends", icon: "⚔️" },
    { id: "valorant", name: "Valorant", icon: "🎯" },
    { id: "cs2", name: "Counter-Strike 2", icon: "💥" },
    { id: "fortnite", name: "Fortnite", icon: "🌪️" },
    { id: "apex", name: "Apex Legends", icon: "🚁" },
  ];

  const categories = [
    { id: "all", name: "Toutes catégories" },
    { id: "coaching", name: "Coaching" },
    { id: "boost", name: "Boost de rang" },
    { id: "carry", name: "Carry" },
    { id: "account", name: "Comptes" },
    { id: "items", name: "Items/Skins" },
  ];

  const services: Service[] = [
    {
      id: 1,
      title: "Coaching Personnalisé Valorant",
      provider: "ValorantMaster",
      game: "valorant",
      category: "coaching",
      price: "30€/h",
      rating: 4.9,
      reviews: 156,
      description:
        "Coaching individuel pour atteindre Radiant. Analyse de gameplay, stratégies avancées.",
      image:
        "https://images.unsplash.com/photo-1605134550917-5fe8cf25a125?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxnYW1pbmclMjBzZXR1cCUyMGNvbG9yZnVsJTIwbmVvbnxlbnwxfHx8fDE3NTg3ODEwODd8MA&ixlib=rb-4.1.0&q=80&w=400",
      badges: ["Pro Player", "Radiant", "Coach Certifié"],
      delivery: "1-2 heures",
      online: true,
    },
    {
      id: 2,
      title: "Boost Rang LoL (Fer à Diamant)",
      provider: "LeagueCarry",
      game: "lol",
      category: "boost",
      price: "20€/div",
      rating: 4.7,
      reviews: 89,
      description:
        "Service de boost rapide et sécurisé. Joueurs Master/Grandmaster uniquement.",
      image:
        "https://images.unsplash.com/photo-1675310854573-c5c8e4089426?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxlc3BvcnRzJTIwZ2FtaW5nJTIwcHJvZmVzc2lvbmFsfGVufDF8fHx8MTc1ODc4MTA5MHww&ixlib=rb-4.1.0&q=80&w=400",
      badges: ["Master", "Boost Vérifié"],
      delivery: "24-48h",
      online: false,
    },
  ];

  const filteredServices = useMemo(() => {
    return services.filter((service) => {
      const matchesSearch =
        service.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        service.provider.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesGame = selectedGame === "all" || service.game === selectedGame;
      const matchesCategory =
        selectedCategory === "all" || service.category === selectedCategory;
      return matchesSearch && matchesGame && matchesCategory;
    });
  }, [searchTerm, selectedGame, selectedCategory]);

  return (
    <ThemedView style={{ flex: 1 }}>
      <MobileHeader />
      <ThemedView style={{ padding: 16, gap: 12 }}>
      <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder }}>
        <ThemedText type="title" style={{ marginBottom: 6 }}>Marketplace Gaming</ThemedText>
        <ThemedText style={{ color: '#9CA3AF' }}>Trouvez le service parfait pour votre jeu</ThemedText>
      </View>

      <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder, gap: 12 }}>
        <TextInput
          placeholder="Rechercher un service..."
          placeholderTextColor="#9CA3AF"
          value={searchTerm}
          onChangeText={setSearchTerm}
          style={{
            width: "100%",
            paddingVertical: 12,
            paddingHorizontal: 14,
            backgroundColor: "rgba(51,65,85,0.5)",
            borderWidth: 1,
            borderColor: theme.slateBorder,
            borderRadius: 10,
            color: theme.text,
          }}
        />

        <View style={{ flexDirection: "row", gap: 8 }}>
          <FlatList
            data={games}
            keyExtractor={(g) => g.id}
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={{ gap: 8 }}
            renderItem={({ item }) => (
              <Pressable
                onPress={() => setSelectedGame(item.id)}
                style={{
                  paddingVertical: 10,
                  paddingHorizontal: 12,
                  borderRadius: 10,
                  borderWidth: selectedGame === item.id ? 1 : 1,
                  borderColor:
                    selectedGame === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                  backgroundColor:
                    selectedGame === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                }}
              >
                <Text style={{ color: theme.text, fontSize: 12 }}>{item.icon} {item.name}</Text>
              </Pressable>
            )}
          />
        </View>

        <View style={{ flexDirection: "row", gap: 8 }}>
          <FlatList
            data={categories}
            keyExtractor={(c) => c.id}
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={{ gap: 8 }}
            renderItem={({ item }) => (
              <Pressable
                onPress={() => setSelectedCategory(item.id)}
                style={{
                  paddingVertical: 10,
                  paddingHorizontal: 12,
                  borderRadius: 10,
                  borderWidth: selectedCategory === item.id ? 1 : 1,
                  borderColor:
                    selectedCategory === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                  backgroundColor:
                    selectedCategory === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                }}
              >
                <Text style={{ color: theme.text, fontSize: 12 }}>{item.name}</Text>
              </Pressable>
            )}
          />
        </View>
      </View>

      <FlatList
        data={filteredServices}
        keyExtractor={(s) => String(s.id)}
        ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
        renderItem={({ item }) => (
          <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, overflow: 'hidden' }}>
            <View style={{ flexDirection: 'row', padding: 12 }}>
              <View style={{ position: 'relative' }}>
                <ImageWithFallback source={item.image} width={80} height={80} borderRadius={10} />
                <View style={{ position: 'absolute', top: 4, right: 4, width: 8, height: 8, borderRadius: 8, backgroundColor: item.online ? theme.green : '#6b7280' }} />
              </View>
              <View style={{ flex: 1, marginLeft: 12 }}>
                <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 }}>
                  <View style={{ flex: 1, paddingRight: 8 }}>
                    <Text style={{ color: theme.text, fontWeight: '600', fontSize: 14 }} numberOfLines={2}>{item.title}</Text>
                    <Text style={{ color: '#a78bfa', fontSize: 12 }}>par {item.provider}</Text>
                  </View>
                  <View style={{ alignItems: 'flex-end' }}>
                    <Text style={{ color: '#34d399', fontWeight: '700', fontSize: 14 }}>{item.price}</Text>
                  </View>
                </View>

                <Text style={{ color: '#D1D5DB', fontSize: 12, marginBottom: 8 }} numberOfLines={2}>{item.description}</Text>

                <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
                  <Text style={{ color: '#FBBF24', fontSize: 12, fontWeight: '600' }}>★ {item.rating} ({item.reviews})</Text>
                  <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.delivery}</Text>
                </View>

                <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                  <View style={{ flexDirection: 'row', gap: 6 }}>
                    {item.badges.slice(0, 2).map((badge, idx) => (
                      <View key={idx} style={{ backgroundColor: 'rgba(147,51,234,0.2)', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999 }}>
                        <Text style={{ color: '#C4B5FD', fontSize: 10 }}>{badge}</Text>
                      </View>
                    ))}
                  </View>
                  <Pressable
                    style={{ backgroundColor: '#6d28d9', paddingHorizontal: 14, paddingVertical: 8, borderRadius: 10 }}
                    onPress={() => {}}
                  >
                    <Text style={{ color: 'white', fontSize: 12, fontWeight: '600' }}>Voir</Text>
                  </Pressable>
                </View>
              </View>
            </View>
          </View>
        )}
      />
      </ThemedView>
    </ThemedView>
  );
}


