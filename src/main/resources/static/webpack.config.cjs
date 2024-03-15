const path = require("path");		// Node.js 内置模块
module.exports = {
    // 设置入口：同级目录的src文件夹内的main.js为入口
    entry:"./js/main.js",
    // 设置输出路径：同级目录的dist文件夹下的bundle.js
    output:{
        // __dirname:当前文件所在路径
        path:path.resolve(__dirname,'./dist'),
        // 打包后的输出文件名
        filename:"mypackage.js"
    }
}
