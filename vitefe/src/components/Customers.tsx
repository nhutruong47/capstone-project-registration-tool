import { Star } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";

export function Customers() {
  const customers = [
    { name: "Nguyễn Văn A", role: "Giám đốc Khách sạn ABC", initials: "NA" },
    { name: "Trần Thị B", role: "Chủ nhà hàng XYZ", initials: "TB" },
    { name: "Lê Văn C", role: "Quản lý Resort DEF", initials: "LC" },
    { name: "Phạm Thị D", role: "CEO Chuỗi khách sạn", initials: "PD" },
    { name: "Hoàng Văn E", role: "Chủ chuỗi cửa hàng", initials: "HE" },
    { name: "Vũ Thị F", role: "Giám đốc Spa", initials: "VF" },
    { name: "Đỗ Văn G", role: "Quản lý Chuỗi F&B", initials: "DG" },
    { name: "Bùi Thị H", role: "Chủ khách sạn boutique", initials: "BH" }
  ];

  return (
    <section className="py-16 bg-white">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Khách hàng của chúng tôi
          </h2>
          <p className="text-gray-600 max-w-2xl mx-auto">
            Được tin dùng bởi hàng ngàn doanh nghiệp trên toàn quốc
          </p>
        </div>
        
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-12">
          {customers.map((customer, index) => (
            <div key={index} className="text-center">
              <Avatar className="w-20 h-20 mx-auto mb-3">
                <AvatarFallback className="bg-emerald-100 text-emerald-700 text-lg">
                  {customer.initials}
                </AvatarFallback>
              </Avatar>
              <h4 className="font-semibold mb-1">{customer.name}</h4>
              <p className="text-sm text-gray-600">{customer.role}</p>
              <div className="flex justify-center gap-1 mt-2">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                ))}
              </div>
            </div>
          ))}
        </div>

        <div className="text-center">
          <p className="text-emerald-600 font-semibold">
            Xem thêm câu chuyện thành công →
          </p>
        </div>
      </div>
    </section>
  );
}
