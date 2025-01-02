n
### 1、打包流程

* 完成 ts 编译（多个文件）
* 通过 webpack (mode=production)打包成一个 js 文件（socket.d.js）
* 发布到 npm

压缩工具(production 后，不需要了)

https://www.wetools.com/js-compress/iYZtYekyTx3sWeGAA6jFE4GjKKnBBzNt

发布说明

https://www.jb51.net/article/278264.htm

### 2、webpack 安装参考

* 全局安装

```
cnpm i -g webpack
cnpm i -g webpack-cli
```

* 项目安装

```
cnpm i -D webpack
cnpm i -D webpack-cli
```

* 装包

```
npm install
```

* 发包

```
npm publish
```


```javascript

/*!
 * FolkMQ v1.8.0-M1
 * (c) 2023-2024 noear.org
 * Released under the AGPL-3.0 License.
 */

```