import React, { useMemo, useState, useEffect } from "react";
import { FlatList, TextInput, View, Text, Pressable } from "react-native";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { useTranslation } from "react-i18next";
import { useRouter } from "expo-router";
import ProductCard from "@/components/ProductCard";
import { fetchProducts, Product } from "@/services/productService";

export default function MarketplaceScreen() {
  const colorScheme = useColorScheme() ?? "light";
  const theme = Colors[colorScheme];
  const { t } = useTranslation('marketplace');
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedGame, setSelectedGame] = useState("all");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [products, setProducts] = useState<Product[]>([]);
  const router = useRouter();

  useEffect(() => {
    let mounted = true;
    fetchProducts().then((list) => {
      if (mounted) setProducts(list);
    });
    return () => { mounted = false };
  }, []);

  const games = [
    { id: "all", name: t('games.all'), icon: "🎮" },
    { id: "lol", name: t('games.lol'), icon: "⚔️" },
    { id: "valorant", name: t('games.valorant'), icon: "🎯" },
    { id: "cs2", name: t('games.cs2'), icon: "💥" },
    { id: "fortnite", name: t('games.fortnite'), icon: "🌪️" },
    { id: "apex", name: t('games.apex'), icon: "🚁" },
  ];

  const categories = [
    { id: "all", name: t('categories.all') },
    { id: "coaching", name: t('categories.coaching') },
    { id: "boost", name: t('categories.boost') },
    { id: "carry", name: t('categories.carry') },
    { id: "account", name: t('categories.account') },
    { id: "items", name: t('categories.items') },
  ];

  const filteredServices = useMemo(() => {
    return products.filter((service) => {
      const matchesSearch =
        service.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        service.provider.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesGame = selectedGame === "all" || service.game === selectedGame;
      const matchesCategory =
        selectedCategory === "all" || service.category === selectedCategory;
      return matchesSearch && matchesGame && matchesCategory;
    });
  }, [searchTerm, selectedGame, selectedCategory, products]);

  return (
    <ThemedView style={{ flex: 1 }}>
      <ThemedView style={{ padding: 16, gap: 12 }}>
        <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder }}>
          <ThemedText type="title" style={{ marginBottom: 6 }}>{t('title')}</ThemedText>
          <ThemedText style={{ color: '#9CA3AF' }}>{t('subtitle')}</ThemedText>
        </View>

        <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder, gap: 12 }}>
          <TextInput
            placeholder={t('search')}
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
                <View>
                  <Pressable
                    onPress={() => setSelectedGame(item.id)}
                    style={{
                      paddingVertical: 10,
                      paddingHorizontal: 12,
                      borderRadius: 10,
                      borderWidth: selectedGame === item.id ? 1 : 1,
                      borderColor: selectedGame === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                      backgroundColor: selectedGame === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                    }}
                  >
                    <Text style={{ color: theme.text, fontSize: 12 }}>{item.icon} {item.name}</Text>
                  </Pressable>
                </View>
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
                    borderColor: selectedCategory === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                    backgroundColor: selectedCategory === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
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
            <ProductCard product={item} onPress={() => router.push(`/product/${item.id}`)} />
          )}
        />
      </ThemedView>
    </ThemedView>
  );
}
