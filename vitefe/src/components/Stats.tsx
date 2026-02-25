export function Stats() {
  const stats = [
    { value: "10,000+", label: "Khách hàng" },
    { value: "50+", label: "Tỉnh thành" },
    { value: "5 năm", label: "Kinh nghiệm" },
    { value: "100%", label: "Hài lòng" }
  ];

  return (
    <section className="py-12 bg-gradient-to-r from-emerald-600 to-teal-600 text-white">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          {stats.map((stat, index) => (
            <div key={index} className="text-center">
              <div className="text-4xl md:text-5xl font-bold mb-2">{stat.value}</div>
              <div className="text-sm md:text-base opacity-90">{stat.label}</div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
