import React, { useMemo, useState, useEffect, useCallback, useRef, memo } from "react";
import { FlatList, TextInput, View, Text, Pressable, ActivityIndicator, ScrollView } from "react-native";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { useTranslation } from "react-i18next";
import { useRouter } from "expo-router";
import ProductCard from "@/components/ProductCard";
import { fetchProducts, fetchProductsByFilters, Product } from "@/services/productService";
import { Ionicons } from '@expo/vector-icons';

// Composant Header mémorisé pour éviter les re-renders
const MarketplaceHeader = memo(({
    theme,
    t,
    searchTerm,
    onSearchChange,
    onClearSearch,
    games,
    selectedGame,
    onGameSelect,
    categories,
    selectedCategory,
    onCategorySelect
}: any) => {
    const searchInputRef = useRef<TextInput>(null);

    return (
        <>
            <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder }}>
                <ThemedText type="title" style={{ marginBottom: 6 }}>{t('title')}</ThemedText>
                <ThemedText style={{ color: '#9CA3AF' }}>{t('subtitle')}</ThemedText>
            </View>

            <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder, gap: 12, marginTop: 12 }}>
                <View style={{ position: 'relative' }}>
                    <View style={{
                        position: 'absolute',
                        left: 14,
                        top: 0,
                        bottom: 0,
                        justifyContent: 'center',
                        zIndex: 1
                    }}>
                        <Ionicons name="search" size={20} color="#9CA3AF" />
                    </View>
                    <TextInput
                        ref={searchInputRef}
                        placeholder={t('search')}
                        placeholderTextColor="#9CA3AF"
                        value={searchTerm}
                        onChangeText={onSearchChange}
                        returnKeyType="search"
                        autoCapitalize="none"
                        autoCorrect={false}
                        blurOnSubmit={false}
                        style={{
                            width: "100%",
                            paddingVertical: 12,
                            paddingLeft: 44,
                            paddingRight: searchTerm ? 44 : 14,
                            backgroundColor: "rgba(51,65,85,0.5)",
                            borderWidth: 1,
                            borderColor: searchTerm ? theme.tint : theme.slateBorder,
                            borderRadius: 10,
                            color: theme.text,
                            fontSize: 14,
                        }}
                    />
                    {searchTerm ? (
                        <Pressable
                            onPress={() => {
                                onClearSearch();
                                setTimeout(() => searchInputRef.current?.focus(), 50);
                            }}
                            style={{
                                position: 'absolute',
                                right: 14,
                                top: 0,
                                bottom: 0,
                                justifyContent: 'center',
                                zIndex: 1,
                                width: 24,
                                height: '100%',
                                alignItems: 'center',
                            }}
                            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                        >
                            <View style={{
                                backgroundColor: 'rgba(156,163,175,0.3)',
                                borderRadius: 12,
                                width: 20,
                                height: 20,
                                justifyContent: 'center',
                                alignItems: 'center',
                            }}>
                                <Ionicons name="close" size={14} color="#9CA3AF" />
                            </View>
                        </Pressable>
                    ) : null}
                </View>

                <ScrollView
                    horizontal
                    showsHorizontalScrollIndicator={false}
                    contentContainerStyle={{ gap: 8 }}
                    keyboardShouldPersistTaps="handled"
                >
                    {games.map((item: any) => (
                        <Pressable
                            key={item.id}
                            onPress={() => onGameSelect(item.id)}
                            style={{
                                paddingVertical: 10,
                                paddingHorizontal: 12,
                                borderRadius: 10,
                                borderWidth: 1,
                                borderColor: selectedGame === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                                backgroundColor: selectedGame === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                            }}
                        >
                            <Text style={{ color: theme.text, fontSize: 12 }}>{item.icon} {item.name}</Text>
                        </Pressable>
                    ))}
                </ScrollView>

                <ScrollView
                    horizontal
                    showsHorizontalScrollIndicator={false}
                    contentContainerStyle={{ gap: 8 }}
                    keyboardShouldPersistTaps="handled"
                >
                    {categories.map((item: any) => (
                        <Pressable
                            key={item.id}
                            onPress={() => onCategorySelect(item.id)}
                            style={{
                                paddingVertical: 10,
                                paddingHorizontal: 12,
                                borderRadius: 10,
                                borderWidth: 1,
                                borderColor: selectedCategory === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                                backgroundColor: selectedCategory === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                            }}
                        >
                            <Text style={{ color: theme.text, fontSize: 12 }}>{item.name}</Text>
                        </Pressable>
                    ))}
                </ScrollView>
            </View>
            <View style={{ height: 12 }} />
        </>
    );
});

MarketplaceHeader.displayName = 'MarketplaceHeader';

export default function MarketplaceScreen() {
    const colorScheme = useColorScheme() ?? "light";
    const theme = Colors[colorScheme];
    const { t } = useTranslation('marketplace');
    const [searchTerm, setSearchTerm] = useState("");
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("");
    const [selectedGame, setSelectedGame] = useState("all");
    const [selectedCategory, setSelectedCategory] = useState("all");
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    // Debounce pour la recherche (300ms)
    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedSearchTerm(searchTerm);
        }, 300);

        return () => clearTimeout(timer);
    }, [searchTerm]);

    useEffect(() => {
        let mounted = true;
        setLoading(true);

        const filters: any = {};
        if (selectedGame !== "all") filters.game = selectedGame;
        if (selectedCategory !== "all") filters.type = selectedCategory;

        const fetchPromise = Object.keys(filters).length > 0
            ? fetchProductsByFilters(filters)
            : fetchProducts();

        fetchPromise.then((list) => {
            if (mounted) {
                setProducts(list);
                setLoading(false);
            }
        }).catch((error) => {
            console.error('Error loading products:', error);
            if (mounted) {
                setProducts([]);
                setLoading(false);
            }
        });

        return () => { mounted = false };
    }, [selectedGame, selectedCategory]);

    const GameDisplayMap: Record<string, string> = {
        all: t('games.all') || "All",
        LEAGUE_OF_LEGENDS: t('games.lol') || "League of Legends",
        TEAMFIGHT_TACTICS: t('games.tft') || "Teamfight Tactics",
        ROCKET_LEAGUE: t('games.rl') || "Rocket League",
        VALORANT: t('games.valorant') || "Valorant",
        OTHER: t('games.other') || "Other",
    };

    const games = [
        { id: "all", name: GameDisplayMap.all, icon: "🎮" },
        { id: "LEAGUE_OF_LEGENDS", name: GameDisplayMap.LEAGUE_OF_LEGENDS, icon: "⚔️" },
        { id: "TEAMFIGHT_TACTICS", name: GameDisplayMap.TEAMFIGHT_TACTICS, icon: "🛡️" },
        { id: "ROCKET_LEAGUE", name: GameDisplayMap.ROCKET_LEAGUE, icon: "🚗" },
        { id: "VALORANT", name: GameDisplayMap.VALORANT, icon: "🎯" },
        { id: "OTHER", name: GameDisplayMap.OTHER, icon: "🔰" },
    ];

    const CategoryDisplayMap: Record<string, string> = {
        all: t('categories.all') || "All",
        BOOST: t('categories.boost') || "Boost",
        COACHING: t('categories.coaching') || "Coaching",
        ACCOUNT_RESALING: t('categories.account') || "Account Resaling",
        OTHER: t('categories.other') || "Other",
    };

    const categories = [
        { id: "all", name: CategoryDisplayMap.all },
        { id: "BOOST", name: CategoryDisplayMap.BOOST },
        { id: "COACHING", name: CategoryDisplayMap.COACHING },
        { id: "ACCOUNT_RESALING", name: CategoryDisplayMap.ACCOUNT_RESALING },
        { id: "OTHER", name: CategoryDisplayMap.OTHER },
    ];

    const filteredServices = useMemo(() => {
        if (!debouncedSearchTerm.trim()) return products;

        const searchLower = debouncedSearchTerm.toLowerCase().trim();
        return products.filter((service) => {
            return (
                service.title?.toLowerCase().includes(searchLower) ||
                service.provider?.toLowerCase().includes(searchLower) ||
                service.description?.toLowerCase().includes(searchLower) ||
                service.game?.toLowerCase().includes(searchLower) ||
                service.category?.toLowerCase().includes(searchLower)
            );
        });
    }, [debouncedSearchTerm, products]);

    // Callbacks pour les handlers - mémorisés pour éviter les re-renders
    const handleSearchChange = useCallback((text: string) => {
        setSearchTerm(text);
    }, []);

    const handleClearSearch = useCallback(() => {
        setSearchTerm("");
    }, []);

    const handleGameSelect = useCallback((gameId: string) => {
        setSelectedGame(gameId);
    }, []);

    const handleCategorySelect = useCallback((categoryId: string) => {
        setSelectedCategory(categoryId);
    }, []);

    return (
        <ThemedView style={{ flex: 1 }}>
            <FlatList
                data={filteredServices}
                keyExtractor={(item, index) => item.id ? `product-${item.id}` : `product-index-${index}`}
                ListHeaderComponent={
                    <MarketplaceHeader
                        theme={theme}
                        t={t}
                        searchTerm={searchTerm}
                        onSearchChange={handleSearchChange}
                        onClearSearch={handleClearSearch}
                        games={games}
                        selectedGame={selectedGame}
                        onGameSelect={handleGameSelect}
                        categories={categories}
                        selectedCategory={selectedCategory}
                        onCategorySelect={handleCategorySelect}
                    />
                }
                keyboardShouldPersistTaps="handled"
                keyboardDismissMode="on-drag"
                ListEmptyComponent={
                    loading ? (
                        <View style={{ padding: 20, alignItems: 'center' }}>
                            <ActivityIndicator size="large" color={theme.tint} />
                            <ThemedText style={{ marginTop: 8, color: '#9CA3AF' }}>
                                {t('loading')}
                            </ThemedText>
                        </View>
                    ) : (
                        <View style={{ padding: 20, alignItems: 'center' }}>
                            <ThemedText style={{ color: '#9CA3AF' }}>
                                {t('noProducts')}
                            </ThemedText>
                        </View>
                    )
                }
                ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
                contentContainerStyle={{ padding: 16 }}
                showsVerticalScrollIndicator={false}
                renderItem={({ item }) => (
                    <ProductCard
                        product={item}
                        onPress={() => {
                            if (item?.id === undefined || item?.id === null) {
                                console.warn('Attempt to open product page but item.id is missing', item);
                                return;
                            }
                            router.push(`/product/${item.id}`);
                        }}

                    />
                )}
            />
        </ThemedView>
    );
}
