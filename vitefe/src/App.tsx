import { Header } from "./components/Header";
import { Hero } from "./components/Hero";
import { Features } from "./components/Features";
import { Benefits } from "./components/Benefits";
import { InfoSection } from "./components/InfoSection";
import { AppPreview } from "./components/AppPreview";
import { Customers } from "./components/Customers";
import { News } from "./components/News";
import { Stats } from "./components/Stats";
import { Footer } from "./components/Footer";

export default function App() {
    return (
        <div className="min-h-screen bg-white">
            <Header />
            <Hero />
            <Features />
            <Benefits />
            <InfoSection />
            <AppPreview />
            <Customers />
            <News />
            <Stats />
            <Footer />
        </div>
    );
}
