import i18n, { type InitOptions } from 'i18next';
import { initReactI18next } from 'react-i18next';
import * as Localization from 'expo-localization';
import AsyncStorage from '@react-native-async-storage/async-storage';
import ChainedBackend from 'i18next-chained-backend';
import resourcesToBackend from 'i18next-resources-to-backend';

// Optional: later you can enable HttpBackend when backend is ready
// import HttpBackend from 'i18next-http-backend';

// Simple AsyncStorage language detector/persistence
const LANGUAGE_STORAGE_KEY = 'app.language';

export async function getStoredLanguage(): Promise<string | null> {
    try {
        const stored = await AsyncStorage.getItem(LANGUAGE_STORAGE_KEY);
        return stored;
    } catch {
        return null;
    }
}

export async function setStoredLanguage(lang: string): Promise<void> {
    try {
        await AsyncStorage.setItem(LANGUAGE_STORAGE_KEY, lang);
    } catch {
        // ignore
    }
}

type LanguageManifest = {
    supportedLngs: string[];
    defaultLng: string;
    labels?: Record<string, string>;
    namespaces?: string[];
};

async function loadLocalManifest(): Promise<LanguageManifest> {
    // Bundled JSON import lets Metro include it in the app
    const manifest: LanguageManifest = require('./locales/languages.json');
    return manifest;
}

export async function initI18n(): Promise<void> {
    if (i18n.isInitialized) return;

    const manifest = await loadLocalManifest();
    const deviceLocales = Localization.getLocales?.() ?? [];
    const devicePrimary = deviceLocales[0]?.languageCode ?? manifest.defaultLng;
    const stored = await getStoredLanguage();
    const initialLng = stored && manifest.supportedLngs.includes(stored)
        ? stored
        : (manifest.supportedLngs.includes(devicePrimary) ? devicePrimary : manifest.defaultLng);

    const useRemote = false; // set to true later and add HttpBackend

    // Metro (React Native bundler) requires static paths. Bundle local JSON explicitly.
    const bundledResources: Record<string, Record<string, any>> = {
        en: {
            common: require('./locales/en/common.json'),
            marketplace: require('./locales/en/marketplace.json'),
            messaging: require('./locales/en/messaging.json'),
            orders: require('./locales/en/orders.json'),
            profile: require('./locales/en/profile.json'),
            modal: require('./locales/en/modal.json')
        },
        fr: {
            common: require('./locales/fr/common.json'),
            marketplace: require('./locales/fr/marketplace.json'),
            messaging: require('./locales/fr/messaging.json'),
            orders: require('./locales/fr/orders.json'),
            profile: require('./locales/fr/profile.json'),
            modal: require('./locales/fr/modal.json')
        },
        de: {
            common: require('./locales/de/common.json'),
            marketplace: require('./locales/de/marketplace.json'),
            messaging: require('./locales/de/messaging.json'),
            orders: require('./locales/de/orders.json'),
            profile: require('./locales/de/profile.json'),
            modal: require('./locales/de/modal.json')
        }
    };

    // Configure chained backends (local-first via bundled map)
    const backends: any[] = [
        resourcesToBackend((lng: string, ns: string) => {
            const byLang = bundledResources[lng];
            const data = byLang ? byLang[ns] : undefined;
            return Promise.resolve(data ?? {});
        })
    ];
    const backendOptions: any[] = [{}];

    // Example of enabling remote later:
    // if (useRemote) {
    //   backends.push(HttpBackend);
    //   backendOptions.push({ loadPath: 'https://your.cdn/locales/{{lng}}/{{ns}}.json' });
    // }

    const initOptions: InitOptions = {
        compatibilityJSON: 'v4',
        lng: initialLng,
        fallbackLng: manifest.defaultLng,
        supportedLngs: manifest.supportedLngs,
        ns: manifest.namespaces ?? ['common'],
        defaultNS: (manifest.namespaces && manifest.namespaces[0]) || 'common',
        interpolation: { escapeValue: false },
        react: { useSuspense: false },
        resources: bundledResources as any,
        backend: {
            backends,
            backendOptions
        }
    };

    await i18n
        .use(ChainedBackend)
        .use(initReactI18next)
        .init({
            ...initOptions,
            backend: initOptions.backend
        });
}

export default i18n;


