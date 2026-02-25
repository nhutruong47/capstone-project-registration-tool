import { Receipt, TrendingUp, Users, Calendar } from "lucide-react";

export function Features() {
  const features = [
    {
      icon: Receipt,
      title: "Quản lý hóa đơn",
      description: "Tạo và quản lý hóa đơn nhanh chóng, chính xác",
      color: "bg-blue-100 text-blue-600"
    },
    {
      icon: TrendingUp,
      title: "Báo cáo doanh thu",
      description: "Theo dõi doanh thu theo thời gian thực",
      color: "bg-green-100 text-green-600"
    },
    {
      icon: Users,
      title: "Quản lý nhân viên",
      description: "Quản lý ca làm và hiệu suất nhân viên",
      color: "bg-purple-100 text-purple-600"
    },
    {
      icon: Calendar,
      title: "Đặt phòng",
      description: "Hệ thống đặt phòng thông minh và tiện lợi",
      color: "bg-orange-100 text-orange-600"
    }
  ];

  return (
    <section className="py-16 bg-gray-50">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Tính năng nổi bật
          </h2>
          <p className="text-gray-600 max-w-2xl mx-auto">
            Hệ thống quản lý toàn diện với những tính năng được thiết kế đặc biệt cho doanh nghiệp của bạn
          </p>
        </div>
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((feature, index) => (
            <div key={index} className="bg-white p-6 rounded-xl shadow-sm hover:shadow-md transition-shadow">
              <div className={`w-12 h-12 ${feature.color} rounded-lg flex items-center justify-center mb-4`}>
                <feature.icon className="h-6 w-6" />
              </div>
              <h3 className="font-semibold text-lg mb-2">{feature.title}</h3>
              <p className="text-gray-600 text-sm">{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
