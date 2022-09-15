

class ApiResponse {
   String? type;//success
   String? date;
   int? spent;
   int? size;
   int? count;
   int? position;
   String? message;
   dynamic data;


   ApiResponse({this.type, this.date, this.spent, this.size, this.count,this.position,this.message,this.data});

   ApiResponse.fromJson(Map<String, dynamic> map) {
      type = map['type'];
      date = map['date'];
      spent = map['spent'];
      size = map['size'];
      count = map['count'];
      position = map['position'];
      message = map['message'];
      type = map['type'];
      data =  map['data'];
   }

}