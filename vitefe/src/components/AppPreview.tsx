import { Button } from "./ui/button";
import { Apple, Smartphone } from "lucide-react";

export function AppPreview() {
  return (
    <section className="py-16 bg-gradient-to-b from-cyan-500 to-blue-600 text-white">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Tải ứng dụng ngay hôm nay
          </h2>
          <p className="text-lg opacity-90 max-w-2xl mx-auto">
            Quản lý doanh nghiệp của bạn mọi lúc, mọi nơi với ứng dụng di động VPO
          </p>
        </div>
        
        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white/10 backdrop-blur-sm rounded-xl p-6 border border-white/20">
            <div className="aspect-[9/16] bg-white/5 rounded-lg mb-4 flex items-center justify-center">
              <Smartphone className="h-24 w-24 text-white/50" />
            </div>
            <p className="text-center font-medium">Màn hình chính</p>
          </div>
          <div className="bg-white/10 backdrop-blur-sm rounded-xl p-6 border border-white/20">
            <div className="aspect-[9/16] bg-white/5 rounded-lg mb-4 flex items-center justify-center">
              <Smartphone className="h-24 w-24 text-white/50" />
            </div>
            <p className="text-center font-medium">Đặt phòng</p>
          </div>
          <div className="bg-white/10 backdrop-blur-sm rounded-xl p-6 border border-white/20">
            <div className="aspect-[9/16] bg-white/5 rounded-lg mb-4 flex items-center justify-center">
              <Smartphone className="h-24 w-24 text-white/50" />
            </div>
            <p className="text-center font-medium">Báo cáo</p>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <Button size="lg" className="bg-black text-white hover:bg-gray-800 gap-2 min-w-[200px]">
            <Apple className="h-5 w-5" />
            <div className="text-left">
              <div className="text-xs">Tải về trên</div>
              <div className="font-semibold">App Store</div>
            </div>
          </Button>
          <Button size="lg" className="bg-black text-white hover:bg-gray-800 gap-2 min-w-[200px]">
            <Smartphone className="h-5 w-5" />
            <div className="text-left">
              <div className="text-xs">Tải về trên</div>
              <div className="font-semibold">Google Play</div>
            </div>
          </Button>
        </div>
      </div>
    </section>
  );
}
