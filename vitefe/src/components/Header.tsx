import { Menu } from "lucide-react";
import { Button } from "./ui/button";

export function Header() {
  return (
    <header className="bg-emerald-600 text-white">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between py-2">
          <div className="flex items-center gap-8">
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 bg-white rounded-full flex items-center justify-center">
                <span className="text-emerald-600 font-bold">VP</span>
              </div>
              <span className="font-semibold text-lg">VPO</span>
            </div>
            <nav className="hidden md:flex items-center gap-6 text-sm">
              <a href="#" className="hover:opacity-80">Trang chủ</a>
              <a href="#" className="hover:opacity-80">Tính năng</a>
              <a href="#" className="hover:opacity-80">Bảng giá</a>
              <a href="#" className="hover:opacity-80">Liên hệ</a>
              <a href="#" className="hover:opacity-80">Hỗ trợ</a>
            </nav>
          </div>
          <div className="flex items-center gap-4">
            <Button variant="ghost" className="text-white hover:bg-emerald-700 hidden md:inline-flex">
              Đăng nhập
            </Button>
            <Button className="bg-white text-emerald-600 hover:bg-gray-100 hidden md:inline-flex">
              Dùng thử miễn phí
            </Button>
            <Button variant="ghost" size="icon" className="md:hidden text-white">
              <Menu className="h-6 w-6" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  );
}
