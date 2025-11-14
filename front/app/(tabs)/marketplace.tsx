import React, { useMemo, useState, useEffect } from "react";
import { FlatList, TextInput, View, Text, Pressable, ActivityIndicator } from "react-native";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { useTranslation } from "react-i18next";
import { useRouter } from "expo-router";
import ProductCard from "@/components/ProductCard";
import { fetchProducts, fetchProductsByFilters, Product } from "@/services/productService";

export default function MarketplaceScreen() {
    const colorScheme = useColorScheme() ?? "light";
    const theme = Colors[colorScheme];
    const { t } = useTranslation('marketplace');
    const [searchTerm, setSearchTerm] = useState("");
    const [selectedGame, setSelectedGame] = useState("all");
    const [selectedCategory, setSelectedCategory] = useState("all");
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

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
        LEAGUE_OF_LEGENDS: t('games.league_of_legends') || "League of Legends",
        TEAMFIGHT_TACTICS: t('games.teamfight_tactics') || "Teamfight Tactics",
        ROCKET_LEAGUE: t('games.rocket_league') || "Rocket League",
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
        ACCOUNT_RESALING: t('categories.account_resaling') || "Account Resaling",
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
        if (!searchTerm) return products;

        return products.filter((service) => {
            const matchesSearch =
                service.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                service.provider.toLowerCase().includes(searchTerm.toLowerCase());
            return matchesSearch;
        });
    }, [searchTerm, products]);

    const renderHeader = () => (
        <>
            <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder }}>
                <ThemedText type="title" style={{ marginBottom: 6 }}>{t('title')}</ThemedText>
                <ThemedText style={{ color: '#9CA3AF' }}>{t('subtitle')}</ThemedText>
            </View>

            <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 12, borderWidth: 1, borderColor: theme.slateBorder, gap: 12, marginTop: 12 }}>
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
                                borderWidth: 1,
                                borderColor: selectedGame === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                                backgroundColor: selectedGame === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                            }}
                        >
                            <Text style={{ color: theme.text, fontSize: 12 }}>{item.icon} {item.name}</Text>
                        </Pressable>
                    )}
                />

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
                                borderWidth: 1,
                                borderColor: selectedCategory === item.id ? "rgba(168,85,247,0.5)" : theme.slateBorder,
                                backgroundColor: selectedCategory === item.id ? "rgba(147,51,234,0.15)" : "rgba(51,65,85,0.5)",
                            }}
                        >
                            <Text style={{ color: theme.text, fontSize: 12 }}>{item.name}</Text>
                        </Pressable>
                    )}
                />
            </View>
            <View style={{ height: 12 }} />
        </>
    );

    return (
        <ThemedView style={{ flex: 1 }}>
            <FlatList
                data={filteredServices}
                keyExtractor={(item, index) => item.id ? `product-${item.id}` : `product-index-${index}`}
                ListHeaderComponent={renderHeader}
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
                            router.push({
                                pathname: '/product/[id]',
                                params: { id: String(item.id) },
                            });
                        }}
                    />
                )}
            />
        </ThemedView>
    );
}
