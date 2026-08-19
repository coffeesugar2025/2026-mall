const { merge } = require('webpack-merge');
const base = require('./webpack.base.conf');

module.exports = merge(base, {
  mode: 'development',
  devServer: {
    port: 8080,
    hot: true,
    historyApiFallback: true,
    headers: { 'Access-Control-Allow-Origin': '*' },
  },
});
