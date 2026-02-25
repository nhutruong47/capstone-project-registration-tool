import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Facebook, Youtube, Mail, Phone, MapPin } from "lucide-react";

export function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="container mx-auto px-4 py-12">
        <div className="grid md:grid-cols-4 gap-8 mb-8">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-10 h-10 bg-emerald-600 rounded-full flex items-center justify-center">
                <span className="text-white font-bold">VP</span>
              </div>
              <span className="font-semibold text-white text-lg">VPO</span>
            </div>
            <p className="text-sm mb-4">
              Giải pháp quản lý toàn diện cho khách sạn và nhà hàng
            </p>
            <div className="flex gap-3">
              <a href="#" className="w-9 h-9 bg-gray-800 rounded-full flex items-center justify-center hover:bg-emerald-600 transition-colors">
                <Facebook className="h-4 w-4" />
              </a>
              <a href="#" className="w-9 h-9 bg-gray-800 rounded-full flex items-center justify-center hover:bg-emerald-600 transition-colors">
                <Youtube className="h-4 w-4" />
              </a>
              <a href="#" className="w-9 h-9 bg-gray-800 rounded-full flex items-center justify-center hover:bg-emerald-600 transition-colors">
                <Mail className="h-4 w-4" />
              </a>
            </div>
          </div>

          <div>
            <h3 className="text-white font-semibold mb-4">Sản phẩm</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white">Tính năng</a></li>
              <li><a href="#" className="hover:text-white">Bảng giá</a></li>
              <li><a href="#" className="hover:text-white">Case Study</a></li>
              <li><a href="#" className="hover:text-white">Đối tác</a></li>
            </ul>
          </div>

          <div>
            <h3 className="text-white font-semibold mb-4">Hỗ trợ</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white">Trung tâm trợ giúp</a></li>
              <li><a href="#" className="hover:text-white">Hướng dẫn sử dụng</a></li>
              <li><a href="#" className="hover:text-white">Video hướng dẫn</a></li>
              <li><a href="#" className="hover:text-white">Liên hệ</a></li>
            </ul>
          </div>

          <div>
            <h3 className="text-white font-semibold mb-4">Liên hệ</h3>
            <ul className="space-y-3 text-sm">
              <li className="flex items-start gap-2">
                <MapPin className="h-4 w-4 mt-0.5 flex-shrink-0" />
                <span>Tầng 10, Tòa nhà ABC, Quận 1, TP.HCM</span>
              </li>
              <li className="flex items-center gap-2">
                <Phone className="h-4 w-4 flex-shrink-0" />
                <span>1900 xxxx</span>
              </li>
              <li className="flex items-center gap-2">
                <Mail className="h-4 w-4 flex-shrink-0" />
                <span>support@vpo.com</span>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 pt-8">
          <div className="md:flex justify-between items-center">
            <div className="mb-4 md:mb-0">
              <p className="text-sm">© 2026 VPO. All rights reserved.</p>
            </div>
            <div className="flex gap-4 text-sm">
              <a href="#" className="hover:text-white">Điều khoản dịch vụ</a>
              <a href="#" className="hover:text-white">Chính sách bảo mật</a>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-emerald-600 py-4">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            <p className="text-white font-semibold">
              Bắt đầu dùng thử miễn phí ngay hôm nay!
            </p>
            <div className="flex gap-2 w-full md:w-auto">
              <Input 
                type="email" 
                placeholder="Email của bạn" 
                className="bg-white max-w-xs"
              />
              <Button className="bg-gray-900 text-white hover:bg-gray-800">
                Đăng ký
              </Button>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
