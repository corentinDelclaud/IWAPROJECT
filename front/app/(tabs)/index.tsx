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

    const username = userInfo?.preferred_username || userInfo?.name || userInfo?.given_name || null;


    return (
        <ScrollView style={{ flex: 1 }}>
            <ThemedView style={{ padding: 16 }}>
                        
            <ThemedView style={{ backgroundColor: theme.slateCard, borderRadius: 14, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
                <ThemedView style={{ alignItems: 'center' }}></ThemedView>
                    <ThemedText type="title" style={{ textAlign: 'center' }}>{t('common:home.title')}</ThemedText>
                    <ThemedText type="title" style={{ textAlign: 'center' }}>{username ? `${username}` : ''}</ThemedText>
                </ThemedView>

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
                        <ThemedText style={{ color: '#FDE68A', fontWeight: '700' }}>{t('common:home.profile.title')}</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>{t('common:home.profile.subtitle')}</ThemedText>
                    </ThemedView>
                </Link>
            </ThemedView>
        </ScrollView>
    );
}
