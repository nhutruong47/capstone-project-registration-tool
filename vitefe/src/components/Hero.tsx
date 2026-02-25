import { Button } from "./ui/button";
import { Check, Users, Building2, BarChart3, ShoppingBag, Store, Utensils } from "lucide-react";
import exampleImage from 'figma:asset/8dc06408a6765160c4d00e6a21ae3c82eb867f31.png';

export function Hero() {
  const features = [
    { icon: Users, label: "Nhân viên" },
    { icon: Building2, label: "Chi nhánh" },
    { icon: BarChart3, label: "Báo cáo" },
    { icon: ShoppingBag, label: "Bán hàng" },
    { icon: Store, label: "Kho hàng" },
    { icon: Utensils, label: "Thực đơn" }
  ];

  return (
    <section className="bg-gradient-to-b from-emerald-600 to-emerald-700 text-white py-16">
      <div className="container mx-auto px-4">
        <div className="grid md:grid-cols-2 gap-8 items-center">
          <div>
            <div className="inline-flex items-center gap-2 bg-red-500 text-white px-4 py-2 rounded-full mb-4">
              <span className="font-bold text-2xl">50%</span>
              <span className="text-sm">Giảm giá đặc biệt</span>
            </div>
            <h1 className="text-4xl md:text-5xl font-bold mb-4">
              Công cụ quản lý khách sạn hiệu quả
            </h1>
            <p className="text-lg mb-6 opacity-90">
              Giải pháp quản lý toàn diện cho khách sạn, nhà hàng và doanh nghiệp của bạn
            </p>
            <div className="flex flex-wrap gap-3 mb-8">
              <Button size="lg" className="bg-white text-emerald-600 hover:bg-gray-100">
                Dùng thử 30 ngày miễn phí
              </Button>
              <Button size="lg" variant="outline" className="border-white text-white hover:bg-emerald-800">
                Xem demo
              </Button>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <Check className="h-5 w-5" />
              <span>Miễn phí 30 ngày</span>
              <Check className="h-5 w-5 ml-4" />
              <span>Không cần thẻ tín dụng</span>
            </div>
          </div>
          <div className="relative">
            <img 
              src={exampleImage}
              alt="Dashboard preview" 
              className="rounded-lg shadow-2xl w-full"
            />
          </div>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mt-12">
          {features.map((feature, index) => (
            <div key={index} className="bg-emerald-500 rounded-lg p-4 text-center hover:bg-emerald-400 transition-colors cursor-pointer">
              <feature.icon className="h-8 w-8 mx-auto mb-2" />
              <p className="text-sm font-medium">{feature.label}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
