/** @jsx React.DOM */

var Title = React.createClass({
  render: function() {
    return <h1>∞∞∞ {this.props.value} ∞∞∞</h1>;
  }
});

React.renderComponent(<Title value="Hackinder" />, document.getElementById('title'));

var Router = Abyssa.Router,
    State = Abyssa.State;

Router({
  home: State('/', {
    enter: function () {
      console.log('Enter home');
    }
  })
})
.configure({
  enableLogs: true,
  notFound: '/'
})
.init();

