import { Check } from "lucide-react";

export function Benefits() {
  const benefits = [
    "Giao diện thân thiện, dễ sử dụng",
    "Tích hợp đa nền tảng",
    "Bảo mật dữ liệu tuyệt đối",
    "Hỗ trợ 24/7",
    "Cập nhật tính năng thường xuyên",
    "Đào tạo miễn phí"
  ];

  return (
    <section className="py-16 bg-white">
      <div className="container mx-auto px-4">
        <div className="bg-gradient-to-r from-emerald-500 to-teal-500 rounded-2xl p-8 md:p-12 text-white">
          <div className="grid md:grid-cols-2 gap-8 items-center">
            <div>
              <h2 className="text-3xl md:text-4xl font-bold mb-4">
                Vì sao chọn chúng tôi?
              </h2>
              <p className="text-lg mb-6 opacity-90">
                Được tin dùng bởi hơn 10,000+ doanh nghiệp trên toàn quốc
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {benefits.map((benefit, index) => (
                  <div key={index} className="flex items-start gap-2">
                    <Check className="h-5 w-5 flex-shrink-0 mt-0.5" />
                    <span className="text-sm">{benefit}</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="bg-white/10 backdrop-blur-sm rounded-xl p-6 border border-white/20">
              <div className="grid grid-cols-2 gap-4 text-center">
                <div className="bg-white/10 rounded-lg p-4">
                  <div className="text-3xl font-bold mb-1">10K+</div>
                  <div className="text-sm opacity-90">Khách hàng</div>
                </div>
                <div className="bg-white/10 rounded-lg p-4">
                  <div className="text-3xl font-bold mb-1">99.9%</div>
                  <div className="text-sm opacity-90">Uptime</div>
                </div>
                <div className="bg-white/10 rounded-lg p-4">
                  <div className="text-3xl font-bold mb-1">24/7</div>
                  <div className="text-sm opacity-90">Hỗ trợ</div>
                </div>
                <div className="bg-white/10 rounded-lg p-4">
                  <div className="text-3xl font-bold mb-1">5★</div>
                  <div className="text-sm opacity-90">Đánh giá</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
