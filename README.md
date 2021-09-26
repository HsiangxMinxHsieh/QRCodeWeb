# 行動裝置QRCODE APP

製作目的：道場點名系統使用QRCODE進行線上簽到, 但隨著功能的增加, 一般的APP無法有相應的調整, 因此需要有一個自行開發的APP, 來支援未來的功能增益。</br>

<a href="https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/features.jpg">點我</a>看詳細說明與未來展望</br>

本專案使用的技術：(供想學習此專案的人參考)</br>

● MVC架構，dataBinding　實作UI </br>
● Kotlin語言撰寫 </br>
● Material Ui Component　按鍵與輸入框效果</br>
● Easy Permissions　取得相機權限</br>
● Retrofit　API送出資料</br>
● Coroutine　另開執行緒 </br>
● zxing　QRcode掃描套件，說明文件<a href="https://github.com/journeyapps/zxing-android-embedded">於此</a> </br>
● Room　輕量化資料庫 </br>
● Lottie　動畫元件導入(主頁掃描動畫與Loading動畫)</br>

<h2>執行方法：</h2>
<h3>一、直接執行</h3>
請直接下載<a href="https://github.com/HsiangxMinxHsieh/QRCodeWeb/tree/master/readme/apk">此處</a>的APK並安裝到Android手機上即可執行

<h3>二、下載源碼後Build與執行：</h3>
(一)請先依照<a href="https://ithelp.ithome.com.tw/articles/10200176">此篇</a>內容下載Android Studio(請下載最新版，因後學所使用gradle版本的關係，造成舊版Android Studio無法執行)</br></br>

(二)兩種方式下載源碼：</br>
<h5>方式1.於Github上直接下載此專案的源碼，如以下圖文說明：</br></h5>
<image width = "90%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E4%B8%8B%E8%BC%89%E6%BA%90%E7%A2%BC%E6%96%B9%E5%BC%8F1_1%262.jpg"/>
(1).點擊code，出現下拉式選單。</br>
(2).點擊下載的ZIP，下載源碼至電腦內。</br></br>
<image width = "90%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E4%B8%8B%E8%BC%89%E6%BA%90%E7%A2%BC%E6%96%B9%E5%BC%8F1_3.jpg"/>
(3).將下載的檔案點擊右鍵後再點擊「解壓縮至此」。</br></br>
<image width = "50%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E4%B8%8B%E8%BC%89%E6%BA%90%E7%A2%BC%E6%96%B9%E5%BC%8F1_4.jpg"/>
(4).回到AndroidStudio，選擇開啟已經存在的專案。</br></br>
<image width = "50%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E4%B8%8B%E8%BC%89%E6%BA%90%E7%A2%BC%E6%96%B9%E5%BC%8F1_5%266.jpg"/>
(5).找到專案存放的位置，滑鼠左鍵點擊它。</br>
(6).按下OK後即可開啟</br></br>

<h5>方式2.直接使用Android Studio內建的Git匯入工具來匯入專案，如以下圖文說明：</br></h5>
<image width = "90%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E4%B8%8B%E8%BC%89%E6%BA%90%E7%A2%BC%E6%96%B9%E5%BC%8F2_1%262.jpg"/>
(1).點擊code，出現下拉式選單。</br>
(2).點擊複製網址位置。</br></br>
<image width = "50%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E4%B8%8B%E8%BC%89%E6%BA%90%E7%A2%BC%E6%96%B9%E5%BC%8F2_3.jpg"/>
(3).回到AndroidStudio，選擇藉由版本管理系統取得專案。</br></br>
<image width = "80%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E4%B8%8B%E8%BC%89%E6%BA%90%E7%A2%BC%E6%96%B9%E5%BC%8F2_4%265.jpg"/>
(4).於紅框處按下Ctrl+V(貼上)。</br>
(5).按下clone以複製整個專案。</br></br>

(三).請等待它自動建置完成後按下紅框處即可執行。(黃框處是目前有連結、已開啟偵錯模式的Android手機。)</br>
<image width = "90%" src = "https://github.com/HsiangxMinxHsieh/QRCodeWeb/blob/master/readme/pic/%E5%95%9F%E5%8B%95%E6%96%B9%E5%BC%8F(%E4%B8%89).jpg"/></br>
