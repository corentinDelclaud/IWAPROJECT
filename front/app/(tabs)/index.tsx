import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Link } from "expo-router";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { MobileHeader } from "@/components/MobileHeader";

export default function HomeScreen() {
    const colorScheme = useColorScheme() ?? 'dark';
    const theme = Colors[colorScheme];

    return (
        <ThemedView style={{ flex: 1 }}>
            <MobileHeader />
            <ThemedView style={{ padding: 16 }}>
            <ThemedView style={{ backgroundColor: theme.slateCard, borderRadius: 14, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
                <ThemedText type="title">Bienvenue 👋</ThemedText>
                <ThemedText style={{ color: '#9CA3AF', marginTop: 4 }}>Accédez rapidement aux sections clés</ThemedText>
            </ThemedView>

            <ThemedView style={{ marginTop: 16, gap: 12 }}>
                <Link href="/(tabs)/marketplace" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(147,51,234,0.15)', borderColor: 'rgba(168,85,247,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#C4B5FD', fontWeight: '700' }}>Marketplace</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>Explorer des services gaming</ThemedText>
                    </ThemedView>
                </Link>
                <Link href="/(tabs)/messaging" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(59,130,246,0.15)', borderColor: 'rgba(59,130,246,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#93C5FD', fontWeight: '700' }}>Messages</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>Discutez avec vos clients</ThemedText>
                    </ThemedView>
                </Link>
                <Link href="/(tabs)/orders" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(34,197,94,0.15)', borderColor: 'rgba(34,197,94,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#86EFAC', fontWeight: '700' }}>Commandes</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>Suivre vos commandes</ThemedText>
                    </ThemedView>
                </Link>
                <Link href="/(tabs)/profile" style={{ display: 'flex' }}>
                    <ThemedView style={{ backgroundColor: 'rgba(245,158,11,0.15)', borderColor: 'rgba(245,158,11,0.3)', borderWidth: 1, padding: 16, borderRadius: 12 }}>
                        <ThemedText style={{ color: '#FDE68A', fontWeight: '700' }}>Avis</ThemedText>
                        <ThemedText style={{ color: '#9CA3AF' }}>Gérer vos évaluations</ThemedText>
                    </ThemedView>
                </Link>
            </ThemedView>
            </ThemedView>
        </ThemedView>
    );
}
