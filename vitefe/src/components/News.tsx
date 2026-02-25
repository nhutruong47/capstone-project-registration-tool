import { ImageWithFallback } from "./figma/ImageWithFallback";
import { Card, CardContent } from "./ui/card";

export function News() {
  const news = [
    {
      title: "Tính năng mới: Tích hợp thanh toán",
      description: "Hỗ trợ đa dạng phương thức thanh toán cho khách hàng của bạn",
      imageUrl: "https://images.unsplash.com/photo-1703355685639-d558d1b0f63e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvZmZpY2UlMjB3b3Jrc3BhY2UlMjBtb2Rlcm58ZW58MXx8fHwxNzcwMTkzODQzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      date: "15/01/2026"
    },
    {
      title: "Case study: Chuỗi khách sạn tăng 200% doanh thu",
      description: "Câu chuyện thành công từ khách hàng sử dụng VPO",
      imageUrl: "https://images.unsplash.com/photo-1605513524006-063ed6ed31e7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzdG9yZSUyMHJldGFpbCUyMGJ1c2luZXNzfGVufDF8fHx8MTc3MDE5Mzk1M3ww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      date: "10/01/2026"
    },
    {
      title: "Hội thảo: Xu hướng quản lý khách sạn 2026",
      description: "Đăng ký tham gia hội thảo miễn phí cùng chuyên gia",
      imageUrl: "https://images.unsplash.com/photo-1551530078-379240770349?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyZXN0YXVyYW50JTIwY2FmZSUyMGludGVyaW9yfGVufDF8fHx8MTc3MDE5Mzk1NHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      date: "05/01/2026"
    }
  ];

  return (
    <section className="py-16 bg-gray-50">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Tin tức
          </h2>
          <p className="text-gray-600 max-w-2xl mx-auto">
            Cập nhật những tin tức mới nhất về sản phẩm và ngành
          </p>
        </div>
        
        <div className="grid md:grid-cols-3 gap-6">
          {news.map((item, index) => (
            <Card key={index} className="overflow-hidden hover:shadow-lg transition-shadow cursor-pointer">
              <div className="aspect-video overflow-hidden">
                <ImageWithFallback 
                  src={item.imageUrl}
                  alt={item.title}
                  className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                />
              </div>
              <CardContent className="p-6">
                <p className="text-sm text-emerald-600 mb-2">{item.date}</p>
                <h3 className="font-semibold text-lg mb-2">{item.title}</h3>
                <p className="text-gray-600 text-sm">{item.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="text-center mt-8">
          <p className="text-emerald-600 font-semibold cursor-pointer hover:underline">
            Xem tất cả tin tức →
          </p>
        </div>
      </div>
    </section>
  );
}
