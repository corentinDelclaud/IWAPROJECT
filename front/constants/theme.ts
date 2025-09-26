/**
 * Below are the colors that are used in the app. The colors are defined in the light and dark mode.
 * There are many other ways to style your app. For example, [Nativewind](https://www.nativewind.dev/), [Tamagui](https://tamagui.dev/), [unistyles](https://reactnativeunistyles.vercel.app), etc.
 */

import { Platform } from 'react-native';

const tintColorLight = '#0a7ea4';
const tintColorDark = '#fff';

export const Colors = {
    light: {
        // text: '#11181C',
        // background: '#fff',
        // tint: tintColorLight,
        // icon: '#687076',
        // tabIconDefault: '#687076',
        // tabIconSelected: tintColorLight,
        // slateBg: '#0f172a',
        // slateCard: 'rgba(30,41,59,0.5)',
        // slateBorder: 'rgba(51,65,85,0.5)',
        // purple: '#7c3aed',
        // blue: '#2563eb',
        // yellow: '#f59e0b',
        // green: '#22c55e',
        text: '#ECEDEE',
        background: '#151718',
        tint: tintColorDark,
        icon: '#9BA1A6',
        tabIconDefault: '#9BA1A6',
        tabIconSelected: tintColorDark,
        slateBg: '#0b1220',
        slateCard: 'rgba(30,41,59,0.5)',
        slateBorder: 'rgba(51,65,85,0.5)',
        purple: '#8b5cf6',
        blue: '#3b82f6',
        yellow: '#fbbf24',
        green: '#22c55e',
    },
    dark: {
        text: '#ECEDEE',
        background: '#151718',
        tint: tintColorDark,
        icon: '#9BA1A6',
        tabIconDefault: '#9BA1A6',
        tabIconSelected: tintColorDark,
        slateBg: '#0b1220',
        slateCard: 'rgba(30,41,59,0.5)',
        slateBorder: 'rgba(51,65,85,0.5)',
        purple: '#8b5cf6',
        blue: '#3b82f6',
        yellow: '#fbbf24',
        green: '#22c55e',
    },
};

export const Fonts = Platform.select({
    ios: {
        /** iOS `UIFontDescriptorSystemDesignDefault` */
        sans: 'system-ui',
        /** iOS `UIFontDescriptorSystemDesignSerif` */
        serif: 'ui-serif',
        /** iOS `UIFontDescriptorSystemDesignRounded` */
        rounded: 'ui-rounded',
        /** iOS `UIFontDescriptorSystemDesignMonospaced` */
        mono: 'ui-monospace',
    },
    default: {
        sans: 'normal',
        serif: 'serif',
        rounded: 'normal',
        mono: 'monospace',
    },
    web: {
        sans: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
        serif: "Georgia, 'Times New Roman', serif",
        rounded: "'SF Pro Rounded', 'Hiragino Maru Gothic ProN', Meiryo, 'MS PGothic', sans-serif",
        mono: "SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace",
    },
});
