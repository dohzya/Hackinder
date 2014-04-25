/** @jsx React.DOM */

var Title = React.createClass({
  render: function() {
    return <h1>∞∞∞ {this.props.value} ∞∞∞</h1>;
  }
});

React.renderComponent(<Title value="Hackinder" />, document.getElementById('title'));
