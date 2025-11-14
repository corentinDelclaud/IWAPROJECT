import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Link } from "expo-router";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/context/AuthContext";
import { View, Text, ScrollView } from "react-native";
import Icon from 'react-native-ico-flags';


export default function HomeScreen() {
    const colorScheme = useColorScheme() ?? 'dark';
    const theme = Colors[colorScheme];
    const { t } = useTranslation();
    const { userInfo, accessToken } = useAuth();

    return (
        <ScrollView style={{ flex: 1 }}>
            <ThemedView style={{ padding: 16 }}>
            
            {/* User Info Debug Card */}
            {userInfo && (
                <ThemedView style={{ backgroundColor: 'rgba(59, 130, 246, 0.1)', borderRadius: 14, padding: 16, borderWidth: 1, borderColor: 'rgba(59, 130, 246, 0.3)', marginBottom: 16 }}>
                    <ThemedText style={{ color: '#60A5FA', fontWeight: '700', fontSize: 16, marginBottom: 8 }}>🔐 Authenticated User Info</ThemedText>
                    <View style={{ gap: 6 }}>
                        <View>
                            <Text style={{ color: '#9CA3AF', fontSize: 11 }}>User ID (sub)</Text>
                            <Text style={{ color: theme.text, fontSize: 13 }}>{userInfo.sub}</Text>
                        </View>
                        {userInfo.email && (
                            <View>
                                <Text style={{ color: '#9CA3AF', fontSize: 11 }}>Email</Text>
                                <Text style={{ color: theme.text, fontSize: 13 }}>{userInfo.email}</Text>
                            </View>
                        )}
                        {userInfo.name && (
                            <View>
                                <Text style={{ color: '#9CA3AF', fontSize: 11 }}>Full Name</Text>
                                <Text style={{ color: theme.text, fontSize: 13 }}>{userInfo.name}</Text>
                            </View>
                        )}
                        {userInfo.preferred_username && (
                            <View>
                                <Text style={{ color: '#9CA3AF', fontSize: 11 }}>Username</Text>
                                <Text style={{ color: theme.text, fontSize: 13 }}>{userInfo.preferred_username}</Text>
                            </View>
                        )}
                        {userInfo.given_name && (
                            <View>
                                <Text style={{ color: '#9CA3AF', fontSize: 11 }}>First Name</Text>
                                <Text style={{ color: theme.text, fontSize: 13 }}>{userInfo.given_name}</Text>
                            </View>
                        )}
                        {userInfo.family_name && (
                            <View>
                                <Text style={{ color: '#9CA3AF', fontSize: 11 }}>Last Name</Text>
                                <Text style={{ color: theme.text, fontSize: 13 }}>{userInfo.family_name}</Text>
                            </View>
                        )}
                        {accessToken && (
                            <View>
                                <Text style={{ color: '#9CA3AF', fontSize: 11 }}>Access Token (first 50 chars)</Text>
                                <Text style={{ color: theme.text, fontSize: 11, fontFamily: 'monospace' }}>{accessToken.substring(0, 50)}...</Text>
                            </View>
                        )}
                    </View>
                </ThemedView>
            )}
            
            <ThemedView style={{ backgroundColor: theme.slateCard, borderRadius: 14, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
                <ThemedText type="title">{t('common:home.title')}</ThemedText>
                <ThemedText style={{ color: '#9CA3AF', marginTop: 4 }}>{t('common:home.subtitle')}</ThemedText>
            </ThemedView>

            <ThemedView style={{ marginTop: 16, gap: 12 }}>
                <Link href="/(tabs)/marketplace" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(147,51,234,0.15)', borderColor: 'rgba(168,85,247,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#C4B5FD', fontWeight: '700' }}>{t('common:home.marketplace.title')}</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>{t('common:home.marketplace.subtitle')}</ThemedText>
                    </ThemedView>
                </Link>
                <Link href="/(tabs)/messaging" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(59,130,246,0.15)', borderColor: 'rgba(59,130,246,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#93C5FD', fontWeight: '700' }}>{t('common:home.messaging.title')}</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>{t('common:home.messaging.subtitle')}</ThemedText>
                    </ThemedView>
                </Link>
                <Link href="/(tabs)/orders" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(34,197,94,0.15)', borderColor: 'rgba(34,197,94,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#86EFAC', fontWeight: '700' }}>{t('common:home.orders.title')}</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>{t('common:home.orders.subtitle')}</ThemedText>
                    </ThemedView>
                </Link>
                <Link href="/(tabs)/profile" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(245,158,11,0.15)', borderColor: 'rgba(245,158,11,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#FDE68A', fontWeight: '700' }}>{t('common:home.reviews.title')}</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>{t('common:home.reviews.subtitle')}</ThemedText>
                    </ThemedView>
                </Link>
            </ThemedView>
            </ThemedView>
        </ScrollView>
    );
}
