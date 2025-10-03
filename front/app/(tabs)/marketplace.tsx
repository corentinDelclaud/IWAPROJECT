import React from "react";
import { FlatList, TextInput, View, Text, Pressable, ScrollView } from "react-native";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { useTranslation } from "react-i18next";
import { useRouter } from "expo-router";
import { useServices, useServiceFilters } from "@/hooks/useProducts";
import { gameFilters, categoryFilters } from "@/data/services";

export default function MarketplaceScreen() {
  const colorScheme = useColorScheme() ?? "light";
  const theme = Colors[colorScheme];
  const { t } = useTranslation('marketplace');
  const router = useRouter();

  // Utilisation des hooks personnalisés
  const { services, loading, error } = useServices();
  const {
    filters,
    filteredServices,
    setSearchTerm,
    setSelectedGame,
    setSelectedCategory
  } = useServiceFilters(services);

  if (loading) {
    return (
      <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ThemedText>Chargement des services...</ThemedText>
      </ThemedView>
    );
  }

  if (error) {
    return (
      <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ThemedText style={{ color: 'red' }}>{error}</ThemedText>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={{ flex: 1 }}>
      <ScrollView
        style={{ flex: 1 }}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ padding: 16, gap: 12 }}
      >
        {/* Header */}
        <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder }}>
          <ThemedText type="title" style={{ marginBottom: 6 }}>{t('title')}</ThemedText>
          <ThemedText style={{ color: '#9CA3AF' }}>{t('subtitle')}</ThemedText>
        </View>

        {/* Filtres */}
        <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder, gap: 12 }}>
          <TextInput
            placeholder={t('search')}
            placeholderTextColor="#9CA3AF"
            value={filters.searchTerm}
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

          {/* Filtres de jeux */}
          <View style={{ height: 50 }}>
            <FlatList
              data={gameFilters}
              keyExtractor={(g) => g.id}
              horizontal
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={{ gap: 8, paddingHorizontal: 4 }}
              renderItem={({ item }) => (
                <Pressable
                  onPress={() => setSelectedGame(item.id)}
                  style={{
                    paddingVertical: 10,
                    paddingHorizontal: 12,
                    borderRadius: 10,
                    borderWidth: 1,
                    borderColor:
                      filters.selectedGame === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                    backgroundColor:
                      filters.selectedGame === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                  }}
                >
                  <Text style={{ color: theme.text, fontSize: 12 }}>{item.icon} {item.name}</Text>
                </Pressable>
              )}
            />
          </View>

          {/* Filtres de catégories */}
          <View style={{ height: 50 }}>
            <FlatList
              data={categoryFilters}
              keyExtractor={(c) => c.id}
              horizontal
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={{ gap: 8, paddingHorizontal: 4 }}
              renderItem={({ item }) => (
                <Pressable
                  onPress={() => setSelectedCategory(item.id)}
                  style={{
                    paddingVertical: 10,
                    paddingHorizontal: 12,
                    borderRadius: 10,
                    borderWidth: 1,
                    borderColor:
                      filters.selectedCategory === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                    backgroundColor:
                      filters.selectedCategory === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                  }}
                >
                  <Text style={{ color: theme.text, fontSize: 12 }}>{item.name}</Text>
                </Pressable>
              )}
            />
          </View>
        </View>

        {/* Liste des services */}
        {filteredServices.map((item, index) => (
          <View key={item.id} style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, overflow: 'hidden' }}>
            <View style={{ flexDirection: 'row', padding: 12 }}>
              <View style={{ position: 'relative' }}>
                <ImageWithFallback source={item.image} width={80} height={80} borderRadius={10} />
                <View style={{ position: 'absolute', top: 4, right: 4, width: 8, height: 8, borderRadius: 8, backgroundColor: item.online ? theme.green : '#6b7280' }} />
              </View>
              <View style={{ flex: 1, marginLeft: 12 }}>
                <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 }}>
                  <View style={{ flex: 1, paddingRight: 8 }}>
                    <Text style={{ color: theme.text, fontWeight: '600', fontSize: 14 }} numberOfLines={2}>{item.title}</Text>
                    <Text style={{ color: '#a78bfa', fontSize: 12 }}>{t('service.by', { provider: item.provider })}</Text>
                  </View>
                  <View style={{ alignItems: 'flex-end' }}>
                    <Text style={{ color: '#34d399', fontWeight: '700', fontSize: 14 }}>{item.price}</Text>
                  </View>
                </View>

                <Text style={{ color: '#D1D5DB', fontSize: 12, marginBottom: 8 }} numberOfLines={2}>{item.description}</Text>

                <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
                  <Text style={{ color: '#FBBF24', fontSize: 12, fontWeight: '600' }}>{t('service.rating', { rating: item.rating, count: item.reviews })}</Text>
                  <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{t('service.delivery', { time: item.delivery })}</Text>
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
                    onPress={() => router.push(`/product/${item.id}`)}
                  >
                    <Text style={{ color: 'white', fontSize: 12, fontWeight: '600' }}>{t('service.view')}</Text>
                  </Pressable>
                </View>
              </View>
            </View>
          </View>
        ))}
      </ScrollView>
    </ThemedView>
  );
}
