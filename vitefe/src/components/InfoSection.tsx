import { ImageWithFallback } from "./figma/ImageWithFallback";

export function InfoSection() {
  const sections = [
    {
      title: "Quản lý toàn diện",
      description: "Hệ thống quản lý tích hợp đầy đủ các tính năng cần thiết cho việc vận hành khách sạn, nhà hàng một cách hiệu quả.",
      points: [
        "Quản lý đặt phòng trực tuyến",
        "Theo dõi tình trạng phòng real-time",
        "Quản lý dịch vụ và tiện ích",
        "Báo cáo chi tiết theo từng bộ phận"
      ],
      imageUrl: "https://images.unsplash.com/photo-1590650589327-3f67c43ad8a2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxidXNpbmVzcyUyMHRlYW0lMjBjb2xsYWJvcmF0aW9uJTIwb2ZmaWNlfGVufDF8fHx8MTc3MDE3NTAwMnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      reverse: false
    },
    {
      title: "Tối ưu hóa doanh thu",
      description: "Phân tích dữ liệu thông minh giúp bạn tối ưu giá phòng, tăng tỷ lệ lấp đầy và tối đa hóa lợi nhuận.",
      points: [
        "Phân tích xu hướng đặt phòng",
        "Gợi ý giá phòng tối ưu",
        "Quản lý khuyến mãi hiệu quả",
        "Dashboard báo cáo trực quan"
      ],
      imageUrl: "https://images.unsplash.com/photo-1759752394755-1241472b589d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBkYXNoYm9hcmQlMjBhbmFseXRpY3MlMjBzY3JlZW58ZW58MXx8fHwxNzcwMTA0NDQ3fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      reverse: true
    },
    {
      title: "Ứng dụng di động",
      description: "Quản lý mọi lúc mọi nơi với ứng dụng di động được tối ưu hóa cho cả iOS và Android.",
      points: [
        "Giao diện thân thiện, dễ sử dụng",
        "Nhận thông báo real-time",
        "Quản lý offline, đồng bộ online",
        "Bảo mật tuyệt đối"
      ],
      imageUrl: "https://images.unsplash.com/photo-1609405985534-c7455cde5d12?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb2JpbGUlMjBhcHAlMjBwaG9uZSUyMGhhbmR8ZW58MXx8fHwxNzcwMTkzOTUzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      reverse: false
    }
  ];

  return (
    <section className="py-16 bg-gray-50">
      <div className="container mx-auto px-4">
        {sections.map((section, index) => (
          <div key={index} className={`grid md:grid-cols-2 gap-8 items-center mb-16 last:mb-0 ${section.reverse ? 'md:flex-row-reverse' : ''}`}>
            <div className={section.reverse ? 'md:order-2' : ''}>
              <h2 className="text-3xl font-bold mb-4">{section.title}</h2>
              <p className="text-gray-600 mb-6">{section.description}</p>
              <ul className="space-y-3">
                {section.points.map((point, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <div className="w-1.5 h-1.5 bg-emerald-500 rounded-full mt-2 flex-shrink-0"></div>
                    <span className="text-gray-700">{point}</span>
                  </li>
                ))}
              </ul>
            </div>
            <div className={section.reverse ? 'md:order-1' : ''}>
              <ImageWithFallback 
                src={section.imageUrl}
                alt={section.title}
                className="rounded-xl shadow-lg w-full"
              />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
